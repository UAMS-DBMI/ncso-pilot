'use strict';

angular.module('ncsoDemo')
    .controller('APIDetailView', function ($scope, $http, $location, $routeParams) {
      // TODO: extract the below url to a config file
      var serviceURL = 'http://localhost:9000/';
      $scope.apiName = $routeParams.name;
      $scope.sqlQuery;
      $scope.sparqlQuery;
      $scope.sparqlResults;

      getJsonData(serviceURL + $scope.apiName);

      function getJsonData (JSONURL) {
        var objectListPromise = $http({method: 'GET', url: JSONURL});

        objectListPromise.then(
          function (data) {
            console.log(data);
            $scope.sqlQuery = data.data.sqlQuery;
            $scope.sparqlQuery = data.data.sparqlQuery;
            $scope.sparqlResults = data.data.sparqlResults;
          }, function (err) {
            console.log('Error !' + err);
          }
        );
      }


    });