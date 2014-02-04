# DocumentHandler

A PhoneGap plugin to handle documents (e.g. PDFs) loaded from a URL. 

## Android

The plugin downloads a document and starts an intent, so that other installed applications can open it.

## iOS

The plugin downloads the document and provides a preview of the document using the Quick Look framework,
including the corresponding actions such as copy, print, etc.