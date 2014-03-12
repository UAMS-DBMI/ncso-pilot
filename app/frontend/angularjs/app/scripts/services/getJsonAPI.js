'use strict';

var app = angular.module('getJsonAPI');

app.factory('getJsonAPI', function ($http) {
  var getJson = function () {
    // This function takes in a url and function
    // The function is what needs to be done to the data output
    // from the url

    this.getData = function (url, functionToApplyToData, title) {
      var promise = this.getPromise(url, title);

      promise.then(
        function (data) {
          functionToApplyToData(data);
        }, function  (err) {
          console.log('Error!' + err);
          throw new Error('Error!' + err);
        }
      );
    };
    
    this.getPromise = function (url, title) {
      if(title!== undefined) {
        return $http({method: 'GET', url: url, title: title});
      } else {
        return $http({method: 'GET', url: url});
      }
    };
  };
  
  return getJson;
});
