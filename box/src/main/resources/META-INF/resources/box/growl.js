(function () {
    'use strict';

    var DURACAO_FADE = 300;

    function obterContainer() {
        var container = document.getElementById('box-growl-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'box-growl-container';
            document.body.appendChild(container);
        }
        return container;
    }

    function fecharToast(toast) {
        if (toast.dataset.boxGrowlFechando === 'true') {
            return;
        }
        toast.dataset.boxGrowlFechando = 'true';
        toast.classList.add('box-growl-toast-saindo');
        setTimeout(function () {
            toast.remove();
        }, DURACAO_FADE);
    }

    function criarToast(mensagem, life, sticky) {
        var toast = document.createElement('div');
        toast.className = 'box-growl-toast box-growl-' + mensagem.severidade;

        var texto = document.createElement('div');
        texto.className = 'box-growl-toast-texto';

        var resumo = document.createElement('strong');
        resumo.textContent = mensagem.resumo;
        texto.appendChild(resumo);

        if (mensagem.detalhe) {
            var detalhe = document.createElement('p');
            detalhe.textContent = mensagem.detalhe;
            texto.appendChild(detalhe);
        }
        toast.appendChild(texto);

        var fechar = document.createElement('button');
        fechar.type = 'button';
        fechar.className = 'box-growl-toast-fechar';
        fechar.setAttribute('aria-label', 'Fechar');
        fechar.textContent = '×';
        fechar.addEventListener('click', function () {
            fecharToast(toast);
        });
        toast.appendChild(fechar);

        obterContainer().appendChild(toast);

        if (!sticky) {
            setTimeout(function () {
                fecharToast(toast);
            }, life);
        }
    }

    function processarWrapper(wrapper) {
        var script = wrapper.querySelector('.box-growl-mensagens');
        // Cada renderizacao do componente (inclusive parcial via ajax) traz
        // um <script> novo com as mensagens daquela requisicao - marca o
        // elemento em si (nao o wrapper) como processado, pra uma
        // rescanned nao duplicar toast se o wrapper nao foi substituido.
        if (!script || script.dataset.boxGrowlProcessado === 'true') {
            return;
        }
        script.dataset.boxGrowlProcessado = 'true';

        var mensagens = JSON.parse(script.textContent);
        if (!mensagens.length) {
            return;
        }
        var life = parseInt(wrapper.dataset.life, 10) || 3000;
        var sticky = wrapper.dataset.sticky === 'true';
        mensagens.forEach(function (mensagem) {
            criarToast(mensagem, life, sticky);
        });
    }

    function iniciarTodos(raiz) {
        (raiz || document).querySelectorAll('.box-growl').forEach(processarWrapper);
    }

    document.addEventListener('DOMContentLoaded', function () {
        iniciarTodos(document);

        // Ver editor.js pro motivo desta checagem ficar aqui dentro.
        if (window.faces && window.faces.ajax && window.faces.ajax.addOnEvent) {
            window.faces.ajax.addOnEvent(function (dados) {
                if (dados.status === 'success') {
                    iniciarTodos(document);
                }
            });
        }
    });
})();
