package ch.ti8m.phonegap.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.webkit.MimeTypeMap;

public class DocumentHandler extends CordovaPlugin {

	public static final String HANDLE_DOCUMENT_ACTION = "HandleDocumentWihtURL";
	public static final int ERROR_NO_HANDLER_FOR_DATA_TYPE = 53;
	public static final int ERROR_UNKNOWN_ERROR = 1;

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (HANDLE_DOCUMENT_ACTION.equals(action)) {
			final JSONObject arg_object = args.getJSONObject(0);
			final String url = arg_object.getString("url");
			System.out.println("Found: " + url);
			cordova.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {

					Context context = cordova.getActivity().getApplicationContext();

					// clean up previous files we downloaded
					clearCacheDirectory();

					// get file bytes. Maybe download them. 
					String base64 = null;
					if(arg_object.has("base64")) {
						try {
							base64 = arg_object.getString("base64");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					byte[] bytes = getBytesForFile(url, base64);
					if(bytes == null) {
						callbackContext.error(ERROR_UNKNOWN_ERROR);
						return;
					}

					// get mime type of file data
					String mimeType = getMimeType(url, bytes);
					if(mimeType == null) {
						callbackContext.error(ERROR_UNKNOWN_ERROR);
						return;
					}

					// create a local file to use as the intent target
					File f = createFileWithBytes(url, bytes);
					if(f == null) {
						callbackContext.error(ERROR_UNKNOWN_ERROR);
						return;
					}

					// start an intent with the file
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(
								Uri.fromFile(f), 
								mimeType);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);

						callbackContext.success(); // Thread-safe.
					}
					catch(ActivityNotFoundException e) {
						// happens when we start intent without something that can handle it
						e.printStackTrace();
						callbackContext.error(ERROR_NO_HANDLER_FOR_DATA_TYPE);
					}
				}
			});
			return true;
		}
		return false;
	}

	private final static String FILE_PREFIX = "DH_";

	private File createFileWithBytes(String url, byte[] bytes) {

		Context context = this.cordova.getActivity().getApplicationContext();

		String extension = MimeTypeMap.getFileExtensionFromUrl(url);

		try {
			File f = File.createTempFile(FILE_PREFIX, extension, context.getExternalFilesDir(null));
			FileOutputStream stream = new FileOutputStream(f);
			stream.write(bytes);
			stream.close();
			return f;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private byte[] getBytesForFile(String url, String data) {
		if(data != null) {
			System.out.println("Lenght of base64: " + data.length());
			return Base64.decode(data, 0);
		}
		else {
			try {
				URL url2 = new URL(url);
				URLConnection conn = url2.openConnection();
				InputStream reader = conn.getInputStream();

				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int readBytes = reader.read(buffer);
				while(readBytes > 0) {
					outStream.write(buffer, 0, readBytes);
				}
				return outStream.toByteArray();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Returns the MIME Type of the file by looking at file 
	 * name extension in the URL. 
	 * @param url
	 * @return
	 */
	private static String getMimeType(String url, byte[] data)
	{
		InputStream inStream = new ByteArrayInputStream(data);
		String mimeType = null;

		try {
			mimeType = URLConnection.guessContentTypeFromStream(inStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(mimeType == null) {
			String extension = MimeTypeMap.getFileExtensionFromUrl(url);
			if (extension != null) {
				MimeTypeMap mime = MimeTypeMap.getSingleton();
				mimeType = mime.getMimeTypeFromExtension(extension);
			}
		}

		System.out.println("Mime Type: " + mimeType);

		return mimeType;
	}


	/**
	 * Removes all files from our private cache directory. 
	 */
	private void clearCacheDirectory() {
		// TODO: implement this
		Context context = this.cordova.getActivity().getApplicationContext();

		File dir = context.getExternalFilesDir(null);
		if(dir == null) {
			return;
		}
		String[] victims = dir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith(FILE_PREFIX);
			}
		});
		for(int i = 0; i < victims.length; i++) {
			File fVictim = new File(dir, victims[i]);
			fVictim.delete();
		}
	}

	private void beep() {
		System.out.println("Hello World");
	}

}
