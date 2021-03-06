'use strict';

angular.module('ncsoDemo')
  .controller('CohortQueryBuilder', function ($scope, $location, $routeParams, HttpHelper, $http, ConfigurationService) {
    // TODO: extract the below url to a config file
    var serviceURL = '';
    if(ConfigurationService.ServicePort !== ''){
      serviceURL = ConfigurationService.ServiceUrl + ':' + ConfigurationService.ServicePort + '/';
    } else {
      serviceURL = ConfigurationService.ServiceUrl + '/';
    }
    $scope.cohortDescriptionText = 'This page helps you identify cohorts of NCS participants that meet certain criteria. Fill out the form and click \"Submit\" to retrieve a cohort of NCS participants that meet your criteria.';
    $scope.exploreDescriptionText = 'This page helps you quickly become familiar with the types of data that the NCS has gathered about its participants. Check the boxes and click \"Submit\" to retrieve specific data. ';

    $scope.cohortParams = {
      anthro: {
        title: 'Set filters using anthropometry data',
        params: [
          {
            fullname: 'BMI (kg/m^2)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            id: 'bmi'
          },
          {
            fullname: 'Length (cm)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            id: 'length'
          },
          {
            fullname: 'Weight (kg)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            id: 'weight'
          },
          {
            fullname: 'Waist Circumference (cm)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            isTODO: true,
            id: 'waist'
          },
          {
            fullname: 'Head Circumference (cm)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            isTODO: true,
            id: 'head'
          },
          {
            fullname: 'Measured Subscapular Skinfold (cm)',
            isChecked: false,
            comparisonOperator: '=',
            value: 0,
            isTODO: true,
            id: 'skinfold'
          }
        ]
      },
      nicotine: {
        title: 'Set filters using nicotine exposure',
        params: [
          {
            fullname: 'Do you want participants who live in a smoking or non-smoking household?',
            id: 'smokingHousehold'
          },
          {
            fullname: 'Do you want participants who live in a household with a smoking or non-smoking mother?',
            isTODO: true,
            id: 'smokingMother'
          },
          {
            fullname: 'Do you want participants who live in a household with a smoking or non-smoking father?',
            isTODO: true,
            id: 'smokeFather'
          }
        ]
      },
      zdata: {
        title: 'Choose types of data to be returned',
        params: [
          {
            fullname: 'Do you want nicotine exposure data?',
            isChecked: false,
            id: 'nicotineData'
          },
          {
            fullname: 'Do you want general health surrogate data?',
            isChecked: false,
            id: 'generalHealthData'
          },
          {
            fullname: 'Do you want anthropometry data?',
            isChecked: false,
            id: 'anthroData'
          },
          {
            fullname: 'Do you want lymphocyte profile data?',
            isChecked: false,
            isTODO: true,
            id: 'lymphoData'
          }
        ]
      }
    };

    $scope.exploratoryCohortParams = {
      list: [
        {
          fullname: 'Do you want nicotine exposure data?',
          isChecked: false,
          id: 'nicotineData'
        },
        {
          fullname: 'Do you want general health surrogate data?',
          isChecked: false,
          id: 'generalHealthData'
        },
        {
          fullname: 'Do you want anthropometry data?',
          isChecked: false,
          id: 'anthroData'
        },
        {
          fullname: 'Do you want lymphocyte profile data?',
          isChecked: false,
          isTODO: true,
          id: 'lymphoData'
        },
        {
          fullname: 'Do you want to include the participant IDs?',
          isChecked: false,
          id: 'participantID'
        }
      ]
    };

    $scope.currentCohortCount = 0;
    $scope.exploratoryCaseCount = 0;

    $scope.submitCohort = function () {
      HttpHelper.httpWrapper({
        url: serviceURL + 'api/cohortbuilder',
        method: 'GET',
        params: {filterData: $scope.cohortParams}
      }, function (data, status, headers, config) {
        $scope.cohortQuery = data.sparqlQuery;

        if (data.sparqlResults !== undefined) {
          $scope.cohortResultKeys = data.sparqlHeaders;
        }
        $scope.cohortResults = data.sparqlResults;
        console.log($scope.cohortResultKeys);
      });
    };

    $scope.submitExplore = function () {
      HttpHelper.httpWrapper({
        url: serviceURL + 'api/cohortexplorer',
        method: 'GET',
        params: {data: $scope.exploratoryCohortParams}
      }, function (data, status, headers, config) {
        console.log(data);
        $scope.exploreQuery = data.sparqlQuery;

        if (data.sparqlResults !== undefined) {
          $scope.exploreResultKeys = data.sparqlHeaders;
        }
        $scope.exploreResults = data.sparqlResults;
        console.log($scope.exploreResultKeys);        
      });
    };

    
  });
