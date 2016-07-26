package ch.ti8m.phonegap.plugins;

import java.io.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;

public class DocumentHandler extends CordovaPlugin {

    public static final String HANDLE_DOCUMENT_ACTION = "HandleDocumentWithURL";
    public static final int ERROR_NO_HANDLER_FOR_DATA_TYPE = 53;
    public static final int ERROR_FILE_NOT_FOUND = 2;
    public static final int ERROR_UNKNOWN_ERROR = 1;

    @Override
    public boolean execute(String action, JSONArray args,
            final CallbackContext callbackContext) throws JSONException {
        if (HANDLE_DOCUMENT_ACTION.equals(action)) {

            // parse arguments
            final JSONObject arg_object = args.getJSONObject(0);
            final String url = arg_object.getString("url");
            final String fileName =arg_object.getString("fileName") ;
            System.out.println("Found: " + url);

            // start async download task
            new FileDownloaderAsyncTask(callbackContext, url, fileName).execute();

            return true;
        }
        return false;
    }

    // used for all downloaded files, so we can find and delete them again.
    private final static String FILE_PREFIX = "DH_";

    /**
     * downloads a file from the given url to external storage.
     *
     * @param url
     * @return
     */
    private File downloadFile(String url, CallbackContext callbackContext) {

        try {
			// get an instance of a cookie manager since it has access to our
            // auth cookie
            CookieManager cookieManager = CookieManager.getInstance();

            // get the cookie string for the site.
            String auth = null;
            if (cookieManager.getCookie(url) != null) {
                auth = cookieManager.getCookie(url).toString();
            }

            URL url2 = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
            if (auth != null) {
                conn.setRequestProperty("Cookie", auth);
            }

            InputStream reader = conn.getInputStream();

            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (extension.equals("")) {
                extension = "pdf";
                System.out.println("extension (default): " + extension);
            }
            File f = File.createTempFile(FILE_PREFIX, "." + extension,
                    null);
            // make sure the receiving app can read this file
            f.setReadable(true, false);
            FileOutputStream outStream = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            int readBytes = reader.read(buffer);
            while (readBytes > 0) {
                outStream.write(buffer, 0, readBytes);
                readBytes = reader.read(buffer);
            }
            reader.close();
            outStream.close();
            return f;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callbackContext.error(ERROR_FILE_NOT_FOUND);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            callbackContext.error(ERROR_UNKNOWN_ERROR);
            return null;
        }
    }

    /**
     * Returns the MIME Type of the file by looking at file name extension in
     * the URL.
     *
     * @param url
     * @return
     */
    private static String getMimeType(String url) {
        String mimeType = null;

        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            mimeType = mime.getMimeTypeFromExtension(extension);
        }

        System.out.println("Mime Type: " + mimeType);

        if (mimeType == null) {
            mimeType = "application/pdf";
            System.out.println("Mime Type (default): " + mimeType);
        }

        return mimeType;
    }

    private class FileDownloaderAsyncTask extends AsyncTask<Void, Void, File> {

        private final CallbackContext callbackContext;
        private final String url;
        private final String fileName;

        public FileDownloaderAsyncTask(CallbackContext callbackContext,
                String url, String fileName) {
            super();
            this.callbackContext = callbackContext;
            this.url = url;
            this.fileName = fileName;
        }

        @Override
        protected File doInBackground(Void... arg0) {
            if (!url.startsWith("file://")) {
                return downloadFile(url, callbackContext);
            } else {
                File file = new File(url.replace("file://", ""));
                return file;
            }
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                // case has already been handled
                return;
            }

            Context context = cordova.getActivity().getApplicationContext();

            // get mime type of file data
            String mimeType = getMimeType(url);
            if (mimeType == null) {
                callbackContext.error(ERROR_UNKNOWN_ERROR);
                return;
            }

            // start an intent with the file
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(result), mimeType);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                callbackContext.success(fileName); // Thread-safe.
            } catch (ActivityNotFoundException e) {
				// happens when we start intent without something that can
                // handle it
                e.printStackTrace();
                callbackContext.error(ERROR_NO_HANDLER_FOR_DATA_TYPE);
            }

        }

    }

}
