'use strict';

angular.module('ncsoDemo')
  .controller('CohortQueryBuilder', function ($scope, $location, $routeParams, getJsonAPI) {
    // TODO: extract the below url to a config file
    var serviceURL = 'http://localhost:9000/';
    var jsonService = new getJsonAPI();
    
    //Cohort params are defined below.
    $scope.cohortParams = {
      anthro: {},
      nicotine: {}
    };

    $scope.exploratoryCohortParams = {
      list: [
      {
        fullname: 'Nicotine Exposure Data',
        isChecked: false
      },
      {
        fullname: 'General Health Surrogate Data',
        isChecked: false
      },
      {
        fullname: 'Anthropometry Data',
        isChecked: false      
      },
      {
        fullname: 'Lymphocyte Profile Data',
        isChecked: false
      },
      {
        fullname: 'Include participant ID in output',
        isChecked: false
      }]
    };
    
    $scope.currentCohortCount = 0;
    $scope.currentExploratoryNCSCohortCount = 0;


    //TODO: remove this. This is just for the demo to show what it might look like
    //This is also very hacky and horrible, but it's the fastest way to change stuff on any scope variable change
    $scope.$watch(function(){
      $scope.currentExploratoryNCSCohortCount = Math.floor(Math.random()*100 + 1);
      $scope.currentCohortCount = Math.floor(Math.random()*100 + 1);
    });


    
  });
