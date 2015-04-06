(function () {
    'use strict';

    /* App Module */

    angular.module('mobileacademy', ['motech-dashboard', 'mobileacademy.controllers', 'mobileacademy.directives', 'mobileacademy.services', 'ngCookies'])
        .config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider.
                when('/helloWorld/', {templateUrl: '../mobileacademy/resources/partials/say-hello.html', controller: 'AcademyHelloWorldController'});
    }]);
}());
