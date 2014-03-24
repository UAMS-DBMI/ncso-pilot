'use strict';

angular.module('ncsoDemo')
  .controller('LandingPage', function ($scope, $http, $location, HttpHelper, ConfigurationService) {
    var serviceURL = ''
    if(ConfigurationService.ServicePort !== ''){
      serviceURL = ConfigurationService.ServiceUrl + ':' + ConfigurationService.ServicePort + '/';
    } else {
      serviceURL = ConfigurationService.ServiceUrl + '/';
    }
    $scope.apiList = [];
    $scope.apiKeys = [];
    $scope.ncsodemoURL = $location.absUrl();

    HttpHelper.httpWrapper({
      method: 'GET',
      url: serviceURL + 'getcurrentapis'
    }, function (data) {
      console.log(data);
      $scope.apiList = data;
    });

  });
