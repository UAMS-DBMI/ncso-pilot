'use strict';

angular.module('ncsoDemo')
  .controller('LandingPage', function ($scope) {

    // TODO: Call the API to list all of the current APIs and save them to a $scope'd variable
    // TODO: Display the above api list and when the user clicks on them:
    //         * Append the following to the url: /apidetail/<apiname>
    //         * Route the current page to the /apidetail/ path and that controller can grab
    //         <apiname> and get the output of the api and shove into the view

    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
