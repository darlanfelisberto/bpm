(function () {
    'use strict';

    // Roda fn(document) no carregamento inicial da pagina e de novo em toda
    // atualizacao parcial (ajax) bem-sucedida - o jeito padrao dos
    // componentes do box se reinicializarem sem duplicar handlers.
    //
    // A checagem de window.faces fica dentro do handler de DOMContentLoaded
    // (nao solta no escopo do modulo) de proposito: o proprio faces.js do
    // Jakarta Faces e incluido DEPOIS do JS de cada componente na pagina
    // (ver @ResourceDependencies), entao checar antes do DOMContentLoaded
    // sempre falhava, silenciosamente. Jakarta Faces 4.x tambem renomeou o
    // objeto global de "jsf" pra "faces" - "jsf" nao existe mais nesta
    // versao.
    function aoProntoOuAjax(fn) {
        document.addEventListener('DOMContentLoaded', function () {
            fn(document);

            if (window.faces && window.faces.ajax && window.faces.ajax.addOnEvent) {
                window.faces.ajax.addOnEvent(function (dados) {
                    if (dados.status === 'success') {
                        fn(document);
                    }
                });
            }
        });
    }

    // Dispara um client behavior (select/move/resize/click) via a mesma API
    // que o proprio f:ajax usa por baixo - "jakarta.faces.behavior.event" e
    // o nome do evento que Schedule.decode()/Schedule2.decode() conferem
    // pra saber qual f:ajax aninhado despachar.
    function disparar(wrapper, nomeEvento, dados) {
        var opcoes = Object.assign({
            execute: '@this',
            render: wrapper.dataset['render' + nomeEvento[0].toUpperCase() + nomeEvento.slice(1)] || '@none',
            'jakarta.faces.behavior.event': nomeEvento
        }, dados);
        window.faces.ajax.request(wrapper.id, null, opcoes);
    }

    window.Box = {
        aoProntoOuAjax: aoProntoOuAjax,
        disparar: disparar
    };
})();
