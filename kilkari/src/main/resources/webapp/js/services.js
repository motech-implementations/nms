(function () {
    'use strict';

    /* Services */

    var services = angular.module('kilkari.services', ['ngResource']);

    services.factory('HelloWorld', function($resource) {
        return $resource('../kilkari/sayHello');
    });
}());
