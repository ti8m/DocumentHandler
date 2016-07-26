var viewDocument = function (
        successHandler,
        failureHandler,
        url, fileName) {
    cordova.exec(
            successHandler,
            failureHandler,
            "DocumentHandler",
            "HandleDocumentWithURL",
            [{"url": url, "fileName":fileName}]);
};

var b64toBlob = function (b64Data, contentType, sliceSize) {
    contentType = contentType || '';
    sliceSize = sliceSize || 512;

    var byteCharacters = atob(b64Data);
    var byteArrays = [];

    for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
        var slice = byteCharacters.slice(offset, offset + sliceSize);

        var byteNumbers = new Array(slice.length);
        for (var i = 0; i < slice.length; i++) {
            byteNumbers[i] = slice.charCodeAt(i);
        }

        var byteArray = new Uint8Array(byteNumbers);

        byteArrays.push(byteArray);
    }

    var blob = new Blob(byteArrays, {type: contentType});
    return blob;
};

var writeBase64ToFile = function (fileName, data, path, type) {
    return new Promise(function (resolve, reject) {
        window.resolveLocalFileSystemURL(path, function (directoryEntry) {
            directoryEntry.getFile(fileName, {create: true}, function (fileEntry) {
                fileEntry.createWriter(function (fileWriter) {
                    var blob = b64toBlob(data, type);
                    fileWriter.write(blob);

                    fileWriter.onwriteend = function (e) {
                        resolve(path + fileName);
                    };

                    fileWriter.onerror = function (e) {
                        reject(e.toString());
                    };
                }, function (error) {
                    reject(error);
                });
            }, function (error) {
                reject(error);
            });
        }, function (error) {
            reject(error);
        });
    });
};

var DocumentViewer = {
    saveAndPreviewBase64File: function (successHandler, failureHandler, data, type, path, fileName) {
        writeBase64ToFile(fileName, data, path, type).then(
                function (response) {
                    viewDocument(successHandler, failureHandler, path + fileName);
                }, function (error) {
            failureHandler('Error');
        });
    },
    previewFileFromUrlOrPath: function (successHandler, failureHandler, url, fileName) {
        viewDocument(successHandler, failureHandler, url, fileName);
    }
};

if (module && module.exports) {
    module.exports = DocumentViewer;
}

