'use strict';


describe('Service: HttpHelper', function () {

  beforeEach(module('HttpHelper'));
  
  var $httpBackend, httpHelper;
  var responseData = 'sample mock response';

  it('should contain $http dependency', inject(function ($http) {
    expect($http).not.toBe(null);
  }));

  describe('HttpHelper http mocking tests', function () {

    beforeEach(inject(function ($injector) {
      $httpBackend = $injector.get('$httpBackend');
      $httpBackend.when('GET', '/test').respond(200, responseData);
      $httpBackend.when('GET', '/notavalidurl').respond(404, {});
      httpHelper = $injector.get('HttpHelper');
    }));
    
    afterEach(function () {
      $httpBackend.flush();
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });
    
    it('should successfully handle a promise for a valid request', function () {
      
      httpHelper.getPromise({
        method: 'GET',
        url: '/test'
      }).success(function (data) {
        expect(data).toEqual(responseData);
      }).error(function (response) {
        //If it ever errors out on the above url then make the test fail
        expect(false).toEqual(true);
      });
    });


    it('should handle passing a function and retrieving the output for a valid request', function () {
      httpHelper.httpWrapper({
        method: 'GET',
        url: '/test'
      }, function (data, status) {
        console.log(data);
        expect(data).not.toBe(null);
        expect(data).not.toBe(undefined);
        expect(data).toEqual(responseData);
        expect(status).toBe(200);
      });
    });
   
    it('getPromise should error out on an incorrect url', function () {
      httpHelper.getPromise({
        method: 'GET',
        url: '/notavalidurl'
      }).success(function (data) {
        expect(false).toEqual(true);
      }).error(function (data) {
        expect(true).toEqual(true);
      });
    });

    it('getData should allow you to set the title of the output', function () {
      var title = 'something really, really cool';
      httpHelper.httpWrapper({
        method: 'GET',
        url: '/test',
        title: title        
      }, function (data, status, headers, config) {
        expect(config.title).toEqual(title);
      });
    });

    it('getPromise should allow you to set the title of the output', function () {
      var title = 'something even cooler';
      httpHelper.getPromise({
        method: 'GET',
        url: '/test',
        title: title
      }).success(function (data, status, headers, config) {
        expect(config.title).toEqual(title);
      }).error(function (data) {
        expect(true).toEqual(false);
      });
    });
  });    
});
