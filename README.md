# Polar Cape Cordova Plugin Document Handler

A PhoneGap plugin to handle documents (e.g. PDFs) loaded from a URL. 

## Plugin dependencies
    
This plugin depends on cordova-plugin-file. If you want to install this plugin you must install first cordova-plugin-file.

    cordova plugin add cordova-plugin-file

## Install

    npm install -g polarcape-cordova-plugin-document-handler
    npm install polarcape-cordova-plugin-document-handler --save-dev
    cordova plugin add polarcape-cordova-plugin-document-handler

## Usage

The plugin exposes two methods on the window object: 
    
     DocumentHandler.previewFileFromUrlOrPath(successHandler, failureHandler, url)

     DocumentHandler.saveAndPreviewBase64File(successHandler, failureHandler, data, type, path, fileName)

The parameters: 

* successHandler: Should be a function. Is called when the file download is done and the file is shown to the user. 
* failureHandler: Should be a function. Is called when there was a problem with downloading the file. 
The function takes an argument which is usually 1 (undefined error). Also see Android section.
* url: A URL to a document. Any cookies the system has for this server are passed along. This ensures that authenticated downloads also work. 
* data: Base64 string that represents the document
* type: Type of the document (Ex. application/pdf )
* path: Should be a path to phone's directory (Android/IOS) where you want to be saved the document before preview. Better use cordova-file-plugin constant to provide correct path.
* fileName: filename is parametar which must contain file extension too. (Ex. 'fileExample.pdf')

## Android

The plugin downloads a document and starts an intent, so that other installed applications can open it.

There is a special failure condition on Android, if the system doesn't have any application that can handle the given MIME type. In this case `failureHandler` will be called with the error code `53` or if link is invalid error code `2`. 

## iOS

The plugin downloads the document and provides a preview of the document using the Quick Look framework,
including the corresponding actions such as copy, print, etc.

## Example 1

    DocumentHandler.previewFileFromUrlOrPath(
        function () {
        console.log('success');
        }, function (error) {
        if (error == 53) {
            console.log('No app that handles this file type.');
        }else if (error == 2){
            console.log('Invalid link');
        }
    },
    'http://www.polyu.edu.hk/iaee/files/pdf-sample.pdf');
    

## Example 2

    DocumentHandler.saveAndPreviewBase64File(
        function (success) {},
        function (error) {
            if (error == 53) {
                console.log('No app that handles this file type.');
            }
        }, 
        'JVBERi0xLjMKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZwovT3V0bGluZXMgMiAwIFIKL1BhZ2VzIDMgMCBSID4+CmVuZG9iagoyIDAgb2JqCjw8IC9UeXBlIC9PdXRsaW5lcyAvQ291bnQgMCA+PgplbmRvYmoKMyAwIG9iago8PCAvVHlwZSAvUGFnZXMKL0tpZHMgWzYgMCBSCl0KL0NvdW50IDEKL1Jlc291cmNlcyA8PAovUHJvY1NldCA0IDAgUgovRm9udCA8PCAKL0YxIDggMCBSCj4+Cj4+Ci9NZWRpYUJveCBbMC4wMDAgMC4wMDAgNjEyLjAwMCA3OTIuMDAwXQogPj4KZW5kb2JqCjQgMCBvYmoKWy9QREYgL1RleHQgXQplbmRvYmoKNSAwIG9iago8PAovQ3JlYXRvciAoRE9NUERGKQovQ3JlYXRpb25EYXRlIChEOjIwMTUwNzIwMTMzMzIzKzAyJzAwJykKL01vZERhdGUgKEQ6MjAxNTA3MjAxMzMzMjMrMDInMDAnKQo+PgplbmRvYmoKNiAwIG9iago8PCAvVHlwZSAvUGFnZQovUGFyZW50IDMgMCBSCi9Db250ZW50cyA3IDAgUgo+PgplbmRvYmoKNyAwIG9iago8PCAvRmlsdGVyIC9GbGF0ZURlY29kZQovTGVuZ3RoIDY2ID4+CnN0cmVhbQp4nOMy0DMwMFBAJovSuZxCFIxN9AwMzRTMDS31DCxNFUJSFPTdDBWMgKIKIWkKCtEaIanFJZqxCiFeCq4hAO4PD0MKZW5kc3RyZWFtCmVuZG9iago4IDAgb2JqCjw8IC9UeXBlIC9Gb250Ci9TdWJ0eXBlIC9UeXBlMQovTmFtZSAvRjEKL0Jhc2VGb250IC9UaW1lcy1Cb2xkCi9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nCj4+CmVuZG9iagp4cmVmCjAgOQowMDAwMDAwMDAwIDY1NTM1IGYgCjAwMDAwMDAwMDggMDAwMDAgbiAKMDAwMDAwMDA3MyAwMDAwMCBuIAowMDAwMDAwMTE5IDAwMDAwIG4gCjAwMDAwMDAyNzMgMDAwMDAgbiAKMDAwMDAwMDMwMiAwMDAwMCBuIAowMDAwMDAwNDE2IDAwMDAwIG4gCjAwMDAwMDA0NzkgMDAwMDAgbiAKMDAwMDAwMDYxNiAwMDAwMCBuIAp0cmFpbGVyCjw8Ci9TaXplIDkKL1Jvb3QgMSAwIFIKL0luZm8gNSAwIFIKPj4Kc3RhcnR4cmVmCjcyNQolJUVPRgo=',
        'application/pdf', 
        cordova.file.dataDirectory, 
        'test.pdf'
    );


