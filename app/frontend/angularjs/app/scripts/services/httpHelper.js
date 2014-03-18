'use strict';

var app = angular.module('HttpHelper');

//This service wraps the angular $http service and allows
//for a centralized location for handeling promises and related errors

//Pass in a function as the dataCallBack parameter
//in order to do something with the results. 
app.service('HttpHelper', function ($http) {
  this.httpWrapper = function (httpConfigObj, dataCallBack) {
    var promise = {};
    
    if(httpConfigObj) {
      promise = this.getPromise(httpConfigObj);
    }
    promise.then(
      function (response) {
        if(dataCallBack) {
          dataCallBack(response.data, response.status, response.headers, response.config);
        }
      }, function (response) {
        console.log('Error!' + response.status);
        throw new Error('Error!' + response.status);
      }
    );
  };
  
  this.getPromise = function (parameterObj) {
    return $http(parameterObj);
  };
});
