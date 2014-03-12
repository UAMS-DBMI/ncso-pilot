'use strict';


describe('Service: getJsonAPI', function () {

  beforeEach(module('getJsonAPI'));
  
  var $httpBackend, jsonService;
  var responseData = 'sample mock response';

  it('should contain $http dependency', inject(function ($http) {
    expect($http).not.toBe(null);
  }));

  describe('getJsonApi http mocking tests', function () {

    beforeEach(inject(function ($injector) {
      $httpBackend = $injector.get('$httpBackend');
      $httpBackend.when('GET', '/test').respond(200, responseData);
      $httpBackend.when('GET', '/notavalidurl').respond(404, {});
      var GetJsonApiService = $injector.get('getJsonAPI');
      jsonService = new GetJsonApiService();
    }));
    
    afterEach(function () {
      $httpBackend.flush();
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });
    
    it('should successfully handle a promise for a valid request', function () {
      
      jsonService.getPromise('/test').success(function (response) {
        expect(response).toEqual(responseData);
      }).error(function (response) {
        //If it ever errors out on the above url then make the test fail
        expect(false).toEqual(true);
      });
    });


    it('should handle passing a function and retrieving the output for a valid request', function () {
      jsonService.getData('/test', function (d) {
        console.log(d.data);
        expect(d.data).not.toBe(null);
        expect(d.data).not.toBe(undefined);
        expect(d.data).toEqual(responseData);
        expect(d.status).toBe(200);
      });
    });
   
    it('getPromise should error out on an incorrect url', function () {
      jsonService.getPromise('/notavalidurl').success(function (response) {
        expect(false).toEqual(true);
      }).error(function (response) {
        expect(true).toEqual(true);
      });
    });
    
    /*it('getData should throw an error on an incorrect url', function () {      
      //TODO: the below doesn't work...
      expect(function () {
        jsonService.getData('/notavalidurl', function (data) {
          console.log(data);
        });
      }).toThrow();
    });*/

    it('getData should allow you to set the title of the output', function () {
      var title = 'something really, really cool';
      jsonService.getData('/test', function (d) {
        expect(d.config.title).toEqual(title);
      }, title);
    });

    it('getPromise should allow you to set the title of the output', function () {
      var title = 'something even cooler';
      jsonService.getPromise('/test', title).success(function (data, status, headers, config) {
        expect(config.title).toEqual(title);
      }).error(function (response) {
        expect(true).toEqual(false);
      });
    });



  });

  
    
});
