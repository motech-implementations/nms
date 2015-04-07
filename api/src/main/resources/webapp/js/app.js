(function () {
    'use strict';

    /* App Module */

    angular.module('api', ['motech-dashboard', 'api.controllers', 'api.directives', 'api.services', 'ngCookies'])
        .config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider.
                when('/helloWorld/', {templateUrl: '../api/resources/partials/say-hello.html', controller: 'apiHelloWorldController'});
    }]);
}());
