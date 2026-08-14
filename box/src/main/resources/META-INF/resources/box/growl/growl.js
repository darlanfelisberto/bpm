(function () {
    'use strict';

    var FADE_DURATION = 300;

    function getContainer() {
        var container = document.getElementById('box-growl-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'box-growl-container';
            document.body.appendChild(container);
        }
        return container;
    }

    function closeToast(toast) {
        if (toast.dataset.boxGrowlClosing === 'true') {
            return;
        }
        toast.dataset.boxGrowlClosing = 'true';
        toast.classList.add('box-growl-toast-leaving');
        setTimeout(function () {
            toast.remove();
        }, FADE_DURATION);
    }

    function createToast(message, life, sticky) {
        var toast = document.createElement('div');
        toast.className = 'box-growl-toast box-growl-' + message.severity;

        var text = document.createElement('div');
        text.className = 'box-growl-toast-text';

        var summary = document.createElement('strong');
        summary.textContent = message.summary;
        text.appendChild(summary);

        if (message.detail) {
            var detail = document.createElement('p');
            detail.textContent = message.detail;
            text.appendChild(detail);
        }
        toast.appendChild(text);

        var closeButton = document.createElement('button');
        closeButton.type = 'button';
        closeButton.className = 'box-growl-toast-close';
        closeButton.setAttribute('aria-label', 'Close');
        closeButton.textContent = '×';
        closeButton.addEventListener('click', function () {
            closeToast(toast);
        });
        toast.appendChild(closeButton);

        getContainer().appendChild(toast);

        if (!sticky) {
            setTimeout(function () {
                closeToast(toast);
            }, life);
        }
    }

    function processWrapper(wrapper) {
        var script = wrapper.querySelector('.box-growl-messages');
        // Every render of the component (including partial via ajax) brings
        // a new <script> with that request's messages - marks the element
        // itself (not the wrapper) as processed, so a rescan doesn't
        // duplicate a toast if the wrapper wasn't replaced.
        if (!script || script.dataset.boxGrowlProcessed === 'true') {
            return;
        }
        script.dataset.boxGrowlProcessed = 'true';

        var messages = JSON.parse(script.textContent);
        if (!messages.length) {
            return;
        }
        var life = parseInt(wrapper.dataset.life, 10) || 3000;
        var sticky = wrapper.dataset.sticky === 'true';
        messages.forEach(function (message) {
            createToast(message, life, sticky);
        });
    }

    function startAll(root) {
        (root || document).querySelectorAll('.box-growl').forEach(processWrapper);
    }

    window.Box.onReadyOrAjax(startAll);
})();
