'use strict';

var app = angular.module('ConfigurationService');

app.service('ConfigurationService', function () {
  this.ServiceUrl = 'http://144.30.12.7';
  this.ServicePort = '9000';
});
