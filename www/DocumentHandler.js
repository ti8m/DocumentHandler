var myFunc = function (
  successHandler, 
  failureHandler, 
  url, 
  base64) {
  cordova.exec(
      successHandler, 
      failureHandler, 
      "DocumentHandler", 
      "HandleDocumentWihtURL", 
      [{"url" : url,
      "base64" : base64}]);
};

window.handleDocumentWithURL = myFunc;

if(module && module.exports) {
  module.exports = myFunc;
}

