(function () {
    'use strict';

    // Runs fn(document) on initial page load and again after every
    // successful partial (ajax) update - how box components re-initialize
    // themselves without duplicating handlers.
    //
    // The window.faces check stays inside the DOMContentLoaded handler
    // (not at module scope) on purpose: Jakarta Faces' own faces.js is
    // included AFTER each component's JS on the page (see
    // @ResourceDependencies), so checking before DOMContentLoaded always
    // failed silently. Jakarta Faces 4.x also renamed the global object
    // from "jsf" to "faces" - "jsf" no longer exists in this version.
    function onReadyOrAjax(fn) {
        document.addEventListener('DOMContentLoaded', function () {
            fn(document);

            if (window.faces && window.faces.ajax && window.faces.ajax.addOnEvent) {
                window.faces.ajax.addOnEvent(function (data) {
                    if (data.status === 'success') {
                        fn(document);
                    }
                });
            }
        });
    }

    // Triggers a client behavior (select/move/resize/click) through the
    // same API f:ajax itself uses under the hood - "jakarta.faces.behavior.event"
    // is the event name Schedule.decode()/Schedule2.decode() check to know
    // which nested f:ajax to dispatch to.
    function trigger(wrapper, eventName, data) {
        var options = Object.assign({
            execute: '@this',
            render: wrapper.dataset['render' + eventName[0].toUpperCase() + eventName.slice(1)] || '@none',
            'jakarta.faces.behavior.event': eventName
        }, data);
        window.faces.ajax.request(wrapper.id, null, options);
    }

    window.Box = {
        onReadyOrAjax: onReadyOrAjax,
        trigger: trigger
    };
})();
