(function () {
    'use strict';

    // Diferente do schedule2.js, aqui nao tem estado nenhum guardado no
    // cliente: toda pagina/ordenacao/filtro sempre bate no servidor (e' o
    // proprio ponto do lazy loading), entao o JS so liga eventos no HTML
    // que o Datatable.java ja renderizou - nada de reconstruir DOM na mao.

    function enviarPagina(wrapper, pagina) {
        var dados = {};
        dados[wrapper.id + '_pagina'] = String(pagina);
        window.Box.disparar(wrapper, 'page', dados);
    }

    function aoClicarBotaoPaginacao(evento) {
        var botao = evento.currentTarget;
        var paginacao = botao.closest('.box-datatable-paginacao');
        var wrapper = botao.closest('.box-datatable');
        var paginaAtual = parseInt(paginacao.dataset.paginaAtual, 10) || 0;
        var totalPaginas = parseInt(paginacao.dataset.totalPaginas, 10) || 1;

        var novaPagina = paginaAtual;
        switch (botao.dataset.acao) {
            case 'primeira':
                novaPagina = 0;
                break;
            case 'anterior':
                novaPagina = Math.max(0, paginaAtual - 1);
                break;
            case 'proxima':
                novaPagina = Math.min(totalPaginas - 1, paginaAtual + 1);
                break;
            case 'ultima':
                novaPagina = totalPaginas - 1;
                break;
        }
        if (novaPagina !== paginaAtual) {
            enviarPagina(wrapper, novaPagina);
        }
    }

    // O campo clicado e' tudo que o servidor precisa - e' ele quem decide o
    // proximo estado (nenhum -> asc -> desc -> nenhum), pra nao duplicar
    // essa regra aqui (ver Datatable.proximaOrdenacao, testado a parte).
    function aoClicarCabecalhoOrdenavel(evento) {
        var th = evento.currentTarget;
        var wrapper = th.closest('.box-datatable');
        var dados = {};
        dados[wrapper.id + '_ordenarPor'] = th.dataset.campo;
        window.Box.disparar(wrapper, 'sort', dados);
    }

    function enviarFiltros(wrapper) {
        var filtros = {};
        wrapper.querySelectorAll('.box-datatable-filtro-input').forEach(function (input) {
            filtros[input.dataset.campo] = input.value;
        });
        var dados = {};
        dados[wrapper.id + '_filtros'] = JSON.stringify(filtros);
        window.Box.disparar(wrapper, 'filter', dados);
    }

    function aoMudarFiltro(evento) {
        enviarFiltros(evento.currentTarget.closest('.box-datatable'));
    }

    function aoTeclarNoFiltro(evento) {
        if (evento.key === 'Enter') {
            // Sem isso, Enter dentro de um input de texto submete o
            // h:form ao redor - o filtro deve ser so ajax, nunca postback
            // de pagina inteira.
            evento.preventDefault();
            enviarFiltros(evento.currentTarget.closest('.box-datatable'));
        }
    }

    function aoClicarLinha(evento) {
        var linha = evento.currentTarget;
        var wrapper = linha.closest('.box-datatable');
        var dados = {};
        dados[wrapper.id + '_linhaId'] = linha.dataset.linhaId || '';
        window.Box.disparar(wrapper, 'select', dados);
    }

    function iniciarDatatable(wrapper) {
        if (wrapper.dataset.boxDatatableIniciado === 'true') {
            return;
        }
        wrapper.dataset.boxDatatableIniciado = 'true';

        wrapper.querySelectorAll('.box-datatable-cabecalho-ordenavel').forEach(function (th) {
            th.addEventListener('click', aoClicarCabecalhoOrdenavel);
        });
        wrapper.querySelectorAll('.box-datatable-paginacao-botao').forEach(function (botao) {
            botao.addEventListener('click', aoClicarBotaoPaginacao);
        });
        wrapper.querySelectorAll('.box-datatable-filtro-input').forEach(function (input) {
            input.addEventListener('change', aoMudarFiltro);
            input.addEventListener('keydown', aoTeclarNoFiltro);
        });
        wrapper.querySelectorAll('.box-datatable-linha').forEach(function (linha) {
            linha.addEventListener('click', aoClicarLinha);
        });
    }

    function iniciarTodos(raiz) {
        (raiz || document).querySelectorAll('.box-datatable').forEach(iniciarDatatable);
    }

    window.Box.aoProntoOuAjax(iniciarTodos);
})();
