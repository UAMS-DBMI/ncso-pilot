'use strict';

var app = angular.module('ConfigurationService');

app.service('ConfigurationService', function () {
  var remoteServer = 'http://ingarden.uams.edu/vantage';
  var localhost = 'http://localhost';
  this.ServiceUrl = remoteServer;
  this.ServicePort = '';
});
