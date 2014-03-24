'use strict';

angular.module('ncsoDemo')
  .controller('APIDetailView', function ($scope, $location, $routeParams, HttpHelper, ConfigurationService) {

    var serviceURL = ''
    if(ConfigurationService.ServicePort !== ''){
        serviceURL = ConfigurationService.ServiceUrl + ':' + ConfigurationService.ServicePort + '/';
    } else {
        serviceURL = ConfigurationService.ServiceUrl + '/';
    }

    $scope.apiName = $routeParams.route;
    $scope.apiPrettyName = $routeParams.prettyName;

    $scope.sqlQuery = '';
    $scope.sparqlQuery = '';
    $scope.sparqlResults = [];
    $scope.colSparqlResults = [];
    $scope.explanationText = '';

    HttpHelper.httpWrapper({
      method: 'GET',
      url: serviceURL + $scope.apiName
    }, function (data) {
      $scope.sqlQuery = data.sqlQuery;
      $scope.sparqlQuery = data.sparqlQuery;
      $scope.sparqlResults = data.sparqlResults;
      $scope.colSparqlResults = Object.getOwnPropertyNames($scope.sparqlResults[0]);
      $scope.explanationText = data.explanation;
      $scope.sqlLegend = data.sqlLegend || 'No SQL legend yet';
      $scope.sparqlLegend = data.sparqlLegend || 'No SPARQL legend yet';
    });

  });
