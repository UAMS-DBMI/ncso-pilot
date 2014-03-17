'use strict';

angular.module('ncsoDemo')
  .controller('LandingPage', function ($scope, $http, $location, getJsonAPI) {
    // TODO: extract the below url to a config file
    var serviceURL = 'http://144.30.12.7:9000/';
    var jsonService = new getJsonAPI();

    $scope.apiList = [];
    $scope.apiKeys = [];
    $scope.ncsodemoURL = $location.absUrl();

    jsonService.getData(serviceURL+ 'getcurrentapis', function (data) {
      $scope.apiList = data.data;
    });

  });
