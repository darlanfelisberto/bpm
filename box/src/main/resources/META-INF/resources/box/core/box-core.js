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

    // User-visible text for every box component, English only - the box
    // lib itself never assumes/ships any other language. An app that wants
    // the UI in another language includes its own small JS, after this
    // file, registering the missing language: Box.messages.pt = {
    // 'confirm.yes': 'Confirmar', ... } (same keys, translated values).
    var messages = {
        en: {
            'confirm.yes': 'Confirm',
            'confirm.no': 'Cancel',
            'confirm.defaultMessage': 'Are you sure?',
            'popup.close': 'Close',
            'growl.close': 'Close',
            'datatable.filterPlaceholder': 'Filter…',
            'datatable.filterBy': 'Filter by {0}',
            'datatable.noRecords': 'No records',
            'datatable.paginationInfo': '{0}–{1} of {2}',
            'autocomplete.noResults': 'No results found',
            'autocomplete.clear': 'Clear selection',
            'autocomplete.loading': 'Loading…'
        }
    };

    // Resolves the text for `key` in the page's own language (document.
    // documentElement.lang, first segment only - "pt-BR" matches a "pt"
    // dictionary), falling back to English when there's no dictionary for
    // that language or the key is missing from it. Extra arguments fill in
    // {0}/{1}/... placeholders, positionally.
    function t(key) {
        var lang = (document.documentElement.lang || 'en').split('-')[0];
        var dictionary = messages[lang] || messages.en;
        var text = dictionary[key] || messages.en[key] || key;
        var args = Array.prototype.slice.call(arguments, 1);
        return text.replace(/\{(\d+)\}/g, function (match, index) {
            return args[index] !== undefined ? args[index] : match;
        });
    }

    window.Box = {
        onReadyOrAjax: onReadyOrAjax,
        trigger: trigger,
        messages: messages,
        t: t
    };
})();
