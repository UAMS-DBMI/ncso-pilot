'use strict';

angular.module('ncsoDemo', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute'
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
      .otherwise({
        redirectTo: '/'
      });
  });
