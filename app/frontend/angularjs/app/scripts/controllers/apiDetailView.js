'use strict';

angular.module('ncsoDemo')
    .controller('APIDetailView', function ($scope, $http, $location, $routeParams, getJsonAPI) {
      // TODO: extract the below url to a config file
      var serviceURL = 'http://localhost:9000/';
      var jsonService = new getJsonAPI();

      $scope.apiName = $routeParams.route;
      $scope.apiPrettyName = $routeParams.prettyName;

      $scope.sqlQuery = '';
      $scope.sparqlQuery = '';
      $scope.sparqlResults = [];
      $scope.colSparqlResults = [];
      $scope.explanationText = '';
      
      jsonService.getData(serviceURL + $scope.apiName, function (data) {
        $scope.sqlQuery = data.data.sqlQuery;
        $scope.sparqlQuery = data.data.sparqlQuery;
        $scope.sparqlResults = data.data.sparqlResults;
        $scope.colSparqlResults = Object.getOwnPropertyNames($scope.sparqlResults[0]);
        $scope.explanationText = data.data.explanation;
        $scope.sqlLegend = data.data.sqlLegend || 'No SQL legend yet';
        $scope.sparqlLegend = data.data.sparqlLegend || 'No SPARQL legend yet';
      });

    });
