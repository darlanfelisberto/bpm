(function () {
    'use strict';

    var FONTS = ['arial', 'georgia', 'times-new-roman', 'courier-new', 'verdana', 'trebuchet-ms'];

    var registeredFonts = false;

    function registerFonts() {
        if (registeredFonts || !window.Quill) {
            return;
        }
        var Font = window.Quill.import('formats/font');
        Font.whitelist = FONTS;
        window.Quill.register(Font, true);
        registeredFonts = true;
    }

    function sync(quill, wrapper) {
        // getSemanticHTML() instead of quill.root.innerHTML: produces real
        // <ul>/<ol> and <pre> and doesn't include the editor's internal
        // markers (e.g. <span class="ql-ui" contenteditable="false"> on
        // list items), which don't make sense outside of Quill.
        wrapper.querySelector('.box-editor-value').value = quill.getSemanticHTML();
    }

    function initEditor(wrapper) {
        if (wrapper.dataset.boxEditorInitialized === 'true') {
            return;
        }
        wrapper.dataset.boxEditorInitialized = 'true';

        var quill = new window.Quill(wrapper.querySelector('.box-editor-quill'), {
            theme: 'snow',
            modules: {
                toolbar: [
                    [{header: [1, 2, 3, false]}],
                    [{font: FONTS}],
                    [{size: ['small', false, 'large', 'huge']}],
                    ['bold', 'italic', 'underline', 'strike'],
                    [{color: []}, {background: []}],
                    [{script: 'sub'}, {script: 'super'}],
                    ['blockquote', 'code-block'],
                    [{list: 'ordered'}, {list: 'bullet'}],
                    [{indent: '-1'}, {indent: '+1'}],
                    [{align: []}],
                    [{direction: 'rtl'}],
                    // "video" left out on purpose: it would produce an
                    // <iframe src="..."> with a free-form URL, outside the
                    // control of the sanitizer's allowlist (see
                    // Editor.java).
                    ['link', 'image'],
                    ['clean']
                ]
            }
        });

        quill.on('text-change', function () {
            sync(quill, wrapper);
        });
    }

    function initAll(root) {
        if (!window.Quill) {
            return;
        }
        registerFonts();
        (root || document).querySelectorAll('.box-editor').forEach(initEditor);
    }

    // Re-initializes editors that appeared in a partial (ajax) update -
    // initEditor() already skips the ones that were already ready. See
    // box-core.js for why the window.faces check stays inside the
    // DOMContentLoaded handler.
    window.Box.onReadyOrAjax(initAll);
})();
