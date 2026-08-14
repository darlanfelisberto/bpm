(function () {
    'use strict';

    // data-box-confirm works standalone, with no Faces component/behavior
    // and no other box resource on the page (see the click listener at the
    // bottom of this file) - so it can't hard-require box-core.js just for
    // Box.t. Uses the shared dictionary when box-core.js happens to be
    // loaded (e.g. because b:confirm or another box component is also on
    // the page), falls back to English otherwise - never breaks either way.
    var FALLBACK = { yes: 'Confirm', no: 'Cancel', defaultMessage: 'Are you sure?' };

    function t(key, fallbackKey) {
        if (window.Box && window.Box.t) {
            return window.Box.t(key);
        }
        return FALLBACK[fallbackKey];
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
    // popup and returns false (stopping the JSF onclick chain); once
    // confirmed, it marks the element and simulates a new click on itself,
    // which this time goes through normally to the ajax/action submission
    // already configured by JSF.
    window.boxConfirm = function (element, event, message) {
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
        yesButton.className = 'box-confirm-yes';
        yesButton.textContent = t('confirm.yes', 'yes');

        var noButton = document.createElement('button');
        noButton.type = 'button';
        noButton.className = 'box-confirm-no';
        noButton.textContent = t('confirm.no', 'no');

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
    };

    // Alternative with no Faces component/behavior at all: any link or
    // button with the data-box-confirm attribute already gets the
    // confirmation, just by rendering the attribute (e.g.
    // data-box-confirm="Delete X?" on top of a plain h:commandLink). A
    // single delegated listener on document, in the capture phase — runs
    // BEFORE the element's own onclick (the one JSF generates for ajax, if
    // any), so it can block it with stopImmediatePropagation() on the first
    // click and let it through normally afterwards once confirmed, with no
    // need for any tag/behavior on the Java side. Coexists without conflict
    // with b:confirm: one uses this listener, the other uses the onclick
    // the behavior itself generates; never both at the same time on the
    // same element.
    document.addEventListener('click', function (event) {
        var element = event.target.closest('[data-box-confirm]');
        if (!element) {
            return;
        }
        var proceeds = window.boxConfirm(element, event, element.getAttribute('data-box-confirm'));
        if (!proceeds) {
            event.stopImmediatePropagation();
        }
    }, true);
})();
