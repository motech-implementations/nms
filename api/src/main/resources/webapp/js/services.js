(function () {
    'use strict';

    /* Services */

    var services = angular.module('api.services', ['ngResource']);

    services.factory('HelloWorld', function($resource) {
        return $resource('../api/sayHello');
    });
}());
