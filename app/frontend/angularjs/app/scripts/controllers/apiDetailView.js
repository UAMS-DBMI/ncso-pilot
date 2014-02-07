'use strict';

angular.module('ncsoDemo')
    .controller('APIDetailView', function ($scope, $http, $location, $routeParams) {
      // TODO: extract the below url to a config file
      var serviceURL = 'http://localhost:9000/';
      $scope.apiName = $routeParams.name;
      $scope.sqlQuery = '';
      $scope.sparqlQuery = '';
      $scope.sparqlResults = [];
      $scope.colSparqlResults = [];
      $scope.explanationText = '';


      function getJsonData (JSONURL) {
        var objectListPromise = $http({method: 'GET', url: JSONURL});

        objectListPromise.then(
          function (data) {
            $scope.sqlQuery = data.data.sqlQuery;
            $scope.sparqlQuery = data.data.sparqlQuery;
            $scope.sparqlResults = data.data.sparqlResults;
            $scope.colSparqlResults = Object.getOwnPropertyNames($scope.sparqlResults[0]);
            $scope.explanationText = data.data.explanation;
          }, function (err) {
            console.log('Error !' + err);
          }
        );
      }

      getJsonData(serviceURL + $scope.apiName);
    });