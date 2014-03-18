'use strict';

angular.module('ncsoDemo', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'getJsonAPI',
  'ui.bootstrap',
  'ConfigurationService'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'LandingPage'
      })
      .when('/apidetail', {
        templateUrl: 'views/detail.html',
        controller: 'APIDetailView'
      })
      .when('/cohortbuilder', {
        templateUrl: 'views/cohort.html',
        controller: 'CohortQueryBuilder'
      })
      .otherwise({
        redirectTo: '/'
      });
  });


angular.module('getJsonAPI', []);
angular.module('ConfigurationService', []);
