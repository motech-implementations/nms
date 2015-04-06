(function () {
    'use strict';

    /* Services */

    var services = angular.module('flw.services', ['ngResource']);

    services.factory('HelloWorld', function($resource) {
        return $resource('../flw/sayHello');
    });
}());
