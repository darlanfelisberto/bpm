(function () {
    'use strict';

    // Click on the native <dialog> backdrop (the ::backdrop pseudo-element)
    // bubbles up with the <dialog> itself as target - it's the only way to
    // detect "clicked outside" without measuring coordinates.
    function onBackdropClick(event) {
        var dialog = event.currentTarget;
        if (event.target === dialog && dialog.dataset.boxPopupClosable !== 'false') {
            dialog.close();
        }
    }

    // Esc fires "cancel" before closing - prevent it when closable=false.
    function onCancel(event) {
        if (event.target.dataset.boxPopupClosable === 'false') {
            event.preventDefault();
        }
    }

    function initPopup(dialog) {
        if (dialog.dataset.boxPopupInitialized === 'true') {
            return;
        }
        dialog.dataset.boxPopupInitialized = 'true';

        dialog.addEventListener('click', onBackdropClick);
        dialog.addEventListener('cancel', onCancel);

        var closeButton = dialog.querySelector('.box-popup-close');
        if (closeButton) {
            closeButton.setAttribute('aria-label', window.Box.t('popup.close'));
            closeButton.addEventListener('click', function () {
                dialog.close();
            });
        }
    }

    function initAll(root) {
        (root || document).querySelectorAll('.box-popup').forEach(initPopup);
    }

    window.Box.popup = {
        open: function (id) {
            var dialog = document.getElementById(id);
            if (dialog) {
                dialog.showModal();
            }
        },
        close: function (id) {
            var dialog = document.getElementById(id);
            if (dialog) {
                dialog.close();
            }
        }
    };

    window.Box.onReadyOrAjax(initAll);
})();
