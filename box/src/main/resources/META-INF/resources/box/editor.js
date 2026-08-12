(function () {
    'use strict';

    function sincronizar(area) {
        var wrapper = area.closest('.box-editor');
        var campoValor = wrapper.querySelector('.box-editor-valor');
        campoValor.value = area.innerHTML;
    }

    function executarComando(area, comando, valor) {
        area.focus();
        document.execCommand(comando, false, valor || null);
        sincronizar(area);
    }

    function inserirImagem(area, arquivo) {
        var leitor = new FileReader();
        leitor.onload = function () {
            area.focus();
            document.execCommand('insertImage', false, leitor.result);
            sincronizar(area);
        };
        leitor.readAsDataURL(arquivo);
    }

    function iniciarEditor(wrapper) {
        if (wrapper.dataset.boxEditorIniciado === 'true') {
            return;
        }
        wrapper.dataset.boxEditorIniciado = 'true';

        var area = wrapper.querySelector('.box-editor-conteudo');

        wrapper.querySelectorAll('[data-box-editor-cmd]').forEach(function (controle) {
            var comando = controle.getAttribute('data-box-editor-cmd');
            if (controle.tagName === 'BUTTON') {
                // mousedown+preventDefault em vez de click: evita que o botão
                // roube o foco/seleção de texto do contenteditable antes do
                // comando rodar (senão o execCommand perde onde aplicar).
                controle.addEventListener('mousedown', function (evento) {
                    evento.preventDefault();
                    executarComando(area, comando);
                });
            } else {
                controle.addEventListener('input', function () {
                    executarComando(area, comando, controle.value);
                });
            }
        });

        area.addEventListener('input', function () {
            sincronizar(area);
        });

        area.addEventListener('paste', function (evento) {
            var itens = (evento.clipboardData || window.clipboardData).items;
            for (var i = 0; i < itens.length; i++) {
                if (itens[i].type.indexOf('image') === 0) {
                    evento.preventDefault();
                    inserirImagem(area, itens[i].getAsFile());
                    return;
                }
            }
            // sem imagem: deixa o paste padrão do navegador acontecer, só
            // sincroniza o valor logo em seguida.
            window.setTimeout(function () {
                sincronizar(area);
            }, 0);
        });
    }

    function iniciarTodos(raiz) {
        (raiz || document).querySelectorAll('.box-editor').forEach(iniciarEditor);
    }

    document.addEventListener('DOMContentLoaded', function () {
        iniciarTodos(document);
    });

    // Reinicializa editores que apareceram numa atualização parcial (ajax) -
    // iniciarEditor() já ignora os que já estavam prontos.
    if (window.jsf && window.jsf.ajax && window.jsf.ajax.addOnEvent) {
        window.jsf.ajax.addOnEvent(function (dados) {
            if (dados.status === 'success') {
                iniciarTodos(document);
            }
        });
    }
})();
