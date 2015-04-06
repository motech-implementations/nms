(function () {
    'use strict';

    /* App Module */

    angular.module('mobilekunji', ['motech-dashboard', 'mobilekunji.controllers', 'mobilekunji.directives', 'mobilekunji.services', 'ngCookies'])
        .config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider.
                when('/helloWorld/', {templateUrl: '../mobilekunji/resources/partials/say-hello.html', controller: 'KunjiHelloWorldController'});
    }]);
}());
