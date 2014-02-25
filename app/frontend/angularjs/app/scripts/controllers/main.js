'use strict';

angular.module('ncsoDemo')
  .controller('LandingPage', function ($scope, $http, $location) {
    // TODO: extract the below url to a config file
    var serviceURL = 'http://localhost:9000/';

    function getJsonData (JSONURL) {
      var objectListPromise = $http({method: 'GET', url: JSONURL});

      objectListPromise.then(
        function (data) {
          $scope.apiList = data.data;
        }, function (err) {
          console.log('Error !' + err);
        }
      );
    }


    $scope.apiList = [];
    $scope.apiKeys = [];
    $scope.ncsodemoURL = $location.absUrl();

    getJsonData(serviceURL + 'getcurrentapis');
  });
