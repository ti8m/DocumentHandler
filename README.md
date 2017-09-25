# DocumentHandler

A PhoneGap plugin to handle documents (e.g. PDFs) loaded from a URL. 

## Usage

The plugin exposes one method on the window object: 

    handleDocumentWithURL(successHandler, failureHandler, url)

The parameters: 

* successHandler: Should be a function. Is called when the file download is done and the file is shown to the user. 
* failureHandler: Should be a function. Is called when there was a problem with downloading the file. 
The function takes an argument which is usually 1 (undefined error). Also see Android section.
* url: A URL to a document. Any cookies the system has for this server are passed along. This ensures that authenticated downloads also work. 

## Android

The plugin downloads a document and starts an intent, so that other installed applications can open it.

There is a special failure condition on Android, if the system doesn't have any application that can handle the given MIME type. In this case `failureHandler` will be called with the error code `53`. 

## iOS

The plugin downloads the document and provides a preview of the document using the Quick Look framework,
including the corresponding actions such as copy, print, etc.

## Events (only iOS)

* documentHandlerOnDismiss - it fires after QuickLook window is closed

## Example usage

    handleDocumentWithURL(
      function() {console.log('success');},
      function(error) {
        console.log('failure');
        if(error == 53) {
          console.log('No app that handles this file type.');
        }
      }, 
      'http://www.example.com/path/to/document.pdf'
    );

    document.addEventListener(
      'documentHandlerOnDismiss',
      function() {
          console.log('document handler was closed');
      },
      false,
    );
