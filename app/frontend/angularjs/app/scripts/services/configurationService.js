'use strict';

var app = angular.module('ConfigurationService');

app.service('ConfigurationService', function () {
  var remoteServer = 'http://144.30.12.7';
  var localhost = 'http://localhost';
  this.ServiceUrl = localhost;
  this.ServicePort = '9000';
});
