'use strict';

angular.module('ncsoDemo')
  .controller('LandingPage', function ($scope, $http, $location, getJsonAPI, ConfigurationService) {
    var serviceURL = ConfigurationService.ServiceUrl + ':' + ConfigurationService.ServicePort + '/';
    var jsonService = new getJsonAPI();

    $scope.apiList = [];
    $scope.apiKeys = [];
    $scope.ncsodemoURL = $location.absUrl();

    jsonService.getData(serviceURL+ 'getcurrentapis', function (data) {
      $scope.apiList = data.data;
    });

  });
