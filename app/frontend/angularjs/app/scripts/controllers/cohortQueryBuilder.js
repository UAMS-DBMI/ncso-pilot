'use strict';

angular.module('ncsoDemo')
  .controller('CohortQueryBuilder', function ($scope, $location, $routeParams, getJsonAPI) {
    // TODO: extract the below url to a config file
    var serviceURL = 'http://localhost:9000/';
    var jsonService = new getJsonAPI();
      
    $scope.cohortParams = {
      anthro: {
        title: 'Anthropometry',
        params : [
          {
            fullname: 'BMI (kg/m^2)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0
          },
          {
            fullname: 'Length (cm)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0
          },
          {
            fullname: 'Weight (kg)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0
          },
          {
            fullname: 'Waist Circumference (cm)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            isTODO: true
          },
          {
            fullname: 'Head Circumference (cm)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            isTODO: true
          },
          {
            fullname: 'Measured Subscapular Skinfold (cm)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            isTODO: true
          }
        ]
      },
      nicotine: {
        title: 'Nicotine Exposure Data',
        params: [
          {
            fullname: 'Does this person live in a Smoking Household?'
          },
          {
            fullname: 'Smoking Mother?',
            isTODO: true
          },
          {
            fullname: 'Smoking Father?',
            isTODO: true
          }
        ]
      }
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
          isChecked: false,
          isTODO: true
        },
        {
          fullname: 'Include participant ID in output',
          isChecked: false
        }
      ]
    };
    
    $scope.currentCohortCount = 0;
    $scope.exploratoryCaseCount = 0;


    //TODO: remove this. This is just for the demo to show what it might look like
    //This is also very hacky and horrible, but it's the fastest way to change stuff on any scope variable change
    $scope.$watch(function(){
      $scope.exploratoryCaseCount = Math.floor(Math.random()*100 + 1);
      $scope.currentCohortCount = Math.floor(Math.random()*100 + 1);
    });
    
    
  });
