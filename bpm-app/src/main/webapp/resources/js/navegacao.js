(function () {
    function destacarSecaoAtual() {
        const linkInicio = document.querySelector('.app-nav-link[data-section="inicio"]');
        if (!linkInicio) return;

        const caminhoAplicacao = linkInicio.pathname.replace(/\/index(?:\.xhtml)?\/?$/, '');
        const caminhoAtual = window.location.pathname;
        const caminhoPagina = caminhoAtual.startsWith(caminhoAplicacao + '/')
            ? caminhoAtual.slice(caminhoAplicacao.length)
            : caminhoAtual;

        let secao;
        if (caminhoPagina === '/' || caminhoPagina === '/index' || caminhoPagina === '/index.xhtml') {
            secao = 'inicio';
        } else if (caminhoPagina.startsWith('/macroprocessos/') || caminhoPagina.startsWith('/processos/')) {
            secao = 'macroprocessos';
        } else if (caminhoPagina === '/avaliacao/instrumentos/instrumento.xhtml'
                || caminhoPagina === '/avaliacao/instrumentos/instrumento') {
            secao = 'instrumentos';
        } else if (caminhoPagina.startsWith('/avaliacao/')) {
            secao = 'avaliacoes';
        }

        if (!secao) return;

        const linkAtivo = document.querySelector('.app-nav-link[data-section="' + secao + '"]');
        if (linkAtivo) {
            linkAtivo.classList.add('is-active');
            linkAtivo.setAttribute('aria-current', 'page');
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', destacarSecaoAtual);
    } else {
        destacarSecaoAtual();
    }
}());
