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

  it('math should work (TODO: add tests)', function () {
    expect(1 + 1).toBe(2);
  });
});
