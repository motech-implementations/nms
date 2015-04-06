(function () {
    'use strict';

    /* App Module */

    angular.module('kilkari', ['motech-dashboard', 'kilkari.controllers', 'kilkari.directives', 'kilkari.services', 'ngCookies'])
        .config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider.
                when('/helloWorld/', {templateUrl: '../kilkari/resources/partials/say-hello.html', controller: 'KilkariHelloWorldController'});
    }]);
}());
