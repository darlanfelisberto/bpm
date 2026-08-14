(function () {
    'use strict';

    // Works standalone, with no Faces component/behavior and no other box
    // resource on the page - so it can't hard-require box-core.js just for
    // Box.t. Uses the shared dictionary when box-core.js happens to be
    // loaded (e.g. because another box component is also on the page),
    // falls back to English otherwise - never breaks either way.
    var FALLBACK = { yes: 'Confirm', no: 'Cancel', defaultMessage: 'Are you sure?' };

    function t(key, fallbackKey) {
        if (window.Box && window.Box.t) {
            return window.Box.t(key);
        }
        return FALLBACK[fallbackKey];
    }

    // Triggering a confirmation needs no Faces component/behavior and no
    // p:namespace passthrough attribute: a real custom element, declared as
    // a plain child inside the link/button. It carries no behavior of its
    // own (just hides itself) - the document-level click listener below
    // reads its attributes when the trigger is clicked. This is what lets
    // EL resolve "message" server-side (it's a literal Facelets attribute)
    // while keeping the whole mechanism 100% client-driven.
    if (!customElements.get('box-confirm')) {
        customElements.define('box-confirm', class extends HTMLElement {
            connectedCallback() {
                this.style.display = 'none';
            }
        });
    }

    var activePopup = null;

    function closePopup() {
        if (activePopup) {
            activePopup.remove();
            activePopup = null;
            document.removeEventListener('click', onClickOutside, true);
            document.removeEventListener('keydown', onKeyDown, true);
        }
    }

    function onClickOutside(event) {
        if (activePopup && !activePopup.contains(event.target)) {
            closePopup();
        }
    }

    function onKeyDown(event) {
        if (event.key === 'Escape') {
            closePopup();
        }
    }

    // Positions the popup centered on the button (pushing it back inside the
    // viewport when necessary) and points the arrow (::before/::after in CSS)
    // at the center of the button, even when the popup had to be shifted.
    function position(popup, element) {
        var linkRect = element.getBoundingClientRect();
        var popupRect = popup.getBoundingClientRect();
        var arrowMargin = 14;

        var spaceBelow = window.innerHeight - linkRect.bottom;
        var fitsBelow = spaceBelow >= popupRect.height + 10;

        var top = fitsBelow
            ? window.scrollY + linkRect.bottom + 10
            : window.scrollY + linkRect.top - popupRect.height - 10;

        var buttonCenter = window.scrollX + linkRect.left + linkRect.width / 2;
        var idealLeft = buttonCenter - popupRect.width / 2;
        var minLeft = window.scrollX + 8;
        var maxLeft = window.scrollX + document.documentElement.clientWidth - popupRect.width - 8;
        var left = Math.min(Math.max(idealLeft, minLeft), maxLeft);

        popup.style.top = top + 'px';
        popup.style.left = left + 'px';
        popup.classList.add(fitsBelow ? 'box-confirm-popup--below' : 'box-confirm-popup--above');

        var arrowLeft = buttonCenter - left;
        arrowLeft = Math.min(Math.max(arrowLeft, arrowMargin), popupRect.width - arrowMargin);
        popup.style.setProperty('--box-arrow', arrowLeft + 'px');
    }

    // Inline confirmation (popup near the element), without blocking the
    // thread like the native confirm(). On the first click it shows the
    // popup and returns false (stopping the JSF onclick chain, via the
    // listener below); once confirmed, it marks the element and simulates a
    // new click on itself, which this time goes through normally to the
    // ajax/action submission already configured by JSF.
    function boxConfirm(element, event, message, options) {
        if (element.dataset.boxConfirmed === 'true') {
            element.dataset.boxConfirmed = 'false';
            return true;
        }

        if (event && event.preventDefault) {
            event.preventDefault();
        }
        closePopup();

        var popup = document.createElement('div');
        popup.className = 'box-confirm-popup';

        var text = document.createElement('p');
        text.textContent = message || t('confirm.defaultMessage', 'defaultMessage');

        var yesButton = document.createElement('button');
        yesButton.type = 'button';
        yesButton.className = 'box-confirm-yes' + (options && options.yesClass ? ' ' + options.yesClass : '');
        yesButton.textContent = (options && options.yesLabel) || t('confirm.yes', 'yes');

        var noButton = document.createElement('button');
        noButton.type = 'button';
        noButton.className = 'box-confirm-no' + (options && options.noClass ? ' ' + options.noClass : '');
        noButton.textContent = (options && options.noLabel) || t('confirm.no', 'no');

        popup.appendChild(text);
        popup.appendChild(yesButton);
        popup.appendChild(noButton);
        document.body.appendChild(popup);
        activePopup = popup;

        position(popup, element);

        yesButton.addEventListener('click', function () {
            closePopup();
            element.dataset.boxConfirmed = 'true';
            element.click();
        });
        noButton.addEventListener('click', closePopup);

        window.setTimeout(function () {
            document.addEventListener('click', onClickOutside, true);
            document.addEventListener('keydown', onKeyDown, true);
        }, 0);

        return false;
    }

    // Delegated listener on document, in the capture phase — runs BEFORE
    // the trigger's own onclick (the one JSF generates for ajax, if any),
    // so it can block it with stopImmediatePropagation() on the first click
    // and let it through normally afterwards once confirmed. No need for
    // any tag/behavior on the Java side: any link or button with a
    // <box-confirm> child already gets the confirmation, just by having the
    // element rendered inside it.
    document.addEventListener('click', function (event) {
        var trigger = event.target.closest('a, button');
        var config = trigger && trigger.querySelector(':scope > box-confirm');
        if (!config) {
            return;
        }

        var options = {
            yesLabel: config.getAttribute('yes-label'),
            yesClass: config.getAttribute('yes-class'),
            noLabel: config.getAttribute('no-label'),
            noClass: config.getAttribute('no-class')
        };

        var proceeds = boxConfirm(trigger, event, config.getAttribute('message') || '', options);
        if (!proceeds) {
            event.stopImmediatePropagation();
        }
    }, true);
})();
