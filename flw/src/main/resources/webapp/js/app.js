(function () {
    'use strict';

    /* App Module */

    angular.module('flw', ['motech-dashboard', 'flw.controllers', 'flw.directives', 'flw.services', 'ngCookies'])
        .config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider.
                when('/helloWorld/', {templateUrl: '../flw/resources/partials/say-hello.html', controller: 'FlwHelloWorldController'});
    }]);
}());
