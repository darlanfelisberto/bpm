(function () {
    'use strict';

    var FONTES = ['arial', 'georgia', 'times-new-roman', 'courier-new', 'verdana', 'trebuchet-ms'];

    var fontesRegistradas = false;

    function registrarFontes() {
        if (fontesRegistradas || !window.Quill) {
            return;
        }
        var Font = window.Quill.import('formats/font');
        Font.whitelist = FONTES;
        window.Quill.register(Font, true);
        fontesRegistradas = true;
    }

    function sincronizar(quill, wrapper) {
        // getSemanticHTML() em vez de quill.root.innerHTML: gera <ul>/<ol>
        // e <pre> de verdade e não inclui os marcadores internos do editor
        // (ex.: <span class="ql-ui" contenteditable="false"> dos itens de
        // lista), que não fazem sentido fora do Quill.
        wrapper.querySelector('.box-editor-valor').value = quill.getSemanticHTML();
    }

    function iniciarEditor(wrapper) {
        if (wrapper.dataset.boxEditorIniciado === 'true') {
            return;
        }
        wrapper.dataset.boxEditorIniciado = 'true';

        var quill = new window.Quill(wrapper.querySelector('.box-editor-quill'), {
            theme: 'snow',
            modules: {
                toolbar: [
                    [{header: [1, 2, 3, false]}],
                    [{font: FONTES}],
                    [{size: ['small', false, 'large', 'huge']}],
                    ['bold', 'italic', 'underline', 'strike'],
                    [{color: []}, {background: []}],
                    [{script: 'sub'}, {script: 'super'}],
                    ['blockquote', 'code-block'],
                    [{list: 'ordered'}, {list: 'bullet'}],
                    [{indent: '-1'}, {indent: '+1'}],
                    [{align: []}],
                    [{direction: 'rtl'}],
                    // "video" fora de propósito: geraria um <iframe
                    // src="..."> com URL livre, fora do controle da lista
                    // de permissão do sanitizador (ver Editor.java).
                    ['link', 'image'],
                    ['clean']
                ]
            }
        });

        quill.on('text-change', function () {
            sincronizar(quill, wrapper);
        });
    }

    function iniciarTodos(raiz) {
        if (!window.Quill) {
            return;
        }
        registrarFontes();
        (raiz || document).querySelectorAll('.box-editor').forEach(iniciarEditor);
    }

    document.addEventListener('DOMContentLoaded', function () {
        iniciarTodos(document);

        // Reinicializa editores que apareceram numa atualização parcial
        // (ajax) - iniciarEditor() já ignora os que já estavam prontos.
        // A checagem de window.faces fica aqui dentro (não solta no fim do
        // arquivo) de propósito: o próprio faces.js do Jakarta Faces é
        // incluído DEPOIS de editor.js na página (ver
        // @ResourceDependencies em Editor.java), então checar antes do
        // DOMContentLoaded sempre falhava, silenciosamente, mesmo com o
        // nome certo. Jakarta Faces 4.x também renomeou o objeto global de
        // "jsf" pra "faces" - "jsf" não existe mais nesta versão.
        if (window.faces && window.faces.ajax && window.faces.ajax.addOnEvent) {
            window.faces.ajax.addOnEvent(function (dados) {
                if (dados.status === 'success') {
                    iniciarTodos(document);
                }
            });
        }
    });
})();
