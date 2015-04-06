(function () {
    'use strict';

    /* Services */

    var services = angular.module('mobilekunji.services', ['ngResource']);

    services.factory('HelloWorld', function($resource) {
        return $resource('../mobilekunji/sayHello');
    });
}());
