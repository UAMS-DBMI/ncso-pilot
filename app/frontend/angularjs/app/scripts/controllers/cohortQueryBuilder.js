'use strict';

angular.module('ncsoDemo')
    .controller('CohortQueryBuilder', function ($scope, $location, $routeParams, getJsonAPI, $http) {
        // TODO: extract the below url to a config file
        var serviceURL = 'http://localhost:9000/';
        var jsonService = new getJsonAPI();

        $scope.cohortDescriptionText = 'This page allows you to identify cohorts of participants that meet multiple criteria.  Fill out the form to retreive a list of NCS participants that meet specific requirements.';
        $scope.exploreDescriptionText = 'This page allows you to easily learn which kind of data NCS captured about its participants. Check the boxes to search specific kinds of data gathered about NCS participants.';

        $scope.cohortParams = {
            anthro: {
                title: 'Set up filters using anthropometry data',
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
                title: 'Set up filters using nicotine exposure',
                params: [
                    {
                        fullname: 'Do you want only participants who live in a smoking household?',
                        id: 'smokingHousehold'
                    },
                    {
                        fullname: 'Do you want only participants who live in a household with a smoking mother?',
                        isTODO: true,
                        id: 'smokingMother'
                    },
                    {
                        fullname: 'Do you want only participants who live in a household with a smoking father?',
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
                    fullname: 'Do you want to the participant IDs?',
                    isChecked: false,
                    id: 'participantID'
                }
            ]
        };

        $scope.currentCohortCount = 0;
        $scope.exploratoryCaseCount = 0;


        //TODO: remove this. This is just for the demo to show what it might look like
        //This is also very hacky and horrible, but it's the fastest way to change stuff on any scope variable change
        /*$scope.$watch(function(){
         $scope.exploratoryCaseCount = Math.floor(Math.random()*100 + 1);
         $scope.currentCohortCount = Math.floor(Math.random()*100 + 1);
         });*/

        $scope.submitCohort = function () {
            var promise = $http({
                url: serviceURL + 'api/cohortbuilder',
                method: "GET",
                params: {filterData: $scope.cohortParams}
            }).success(function (data, status, headers, config) {
                $scope.cohortQuery = data.sparqlQuery;

                if(data.sparqlResults !== undefined) {
                    $scope.cohortResultKeys = Object.getOwnPropertyNames(data.sparqlResults[0])
                }
                $scope.cohortResults = data.sparqlResults
                console.log($scope.cohortResultKeys)
            }).error(function (data, status, headers, config) {
                console.log("Error: " + status)
            });

        }

        $scope.submitExplore = function () {
            $http({
                url: serviceURL + 'api/cohortexplorer',
                method: "GET",
                params: {data: $scope.exploratoryCohortParams}
            }).success(function (data, status, headers, config) {
                $scope.exploreQuery = data.sparqlQuery;

                if(data.sparqlResults !== undefined) {
                    $scope.exploreResultKeys = Object.getOwnPropertyNames(data.sparqlResults[0])
                }
                $scope.exploreResults = data.sparqlResults
                console.log($scope.exploreResultKeys)

            }).error(function (data, status, headers, config) {
                console.log("Error: " + status)
            });


        }

    });
