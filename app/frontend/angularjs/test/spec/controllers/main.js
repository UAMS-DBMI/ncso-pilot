'use strict';

describe('Controller: LandingPage', function () {

  // load the controller's module
  beforeEach(module('ncsoDemo'));

  var LandingPage,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    LandingPage = $controller('LandingPage', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
