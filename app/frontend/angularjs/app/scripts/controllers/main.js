'use strict';

angular.module('ncsoDemo')
  .controller('LandingPage', function ($scope, $http, $location, getJsonAPI) {
    // TODO: extract the below url to a config file
    var serviceURL = 'http://localhost:9000/';
    var jsonService = new getJsonAPI();

    $scope.apiList = [];
    $scope.apiKeys = [];
    $scope.ncsodemoURL = $location.absUrl();

    jsonService.getData(serviceURL+ 'getcurrentapis', function (data) {
      $scope.apiList = data.data;
    });

  });
