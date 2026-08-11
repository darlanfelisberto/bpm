(function () {
    'use strict';

    var popupAtivo = null;

    function fecharPopup() {
        if (popupAtivo) {
            popupAtivo.remove();
            popupAtivo = null;
            document.removeEventListener('click', aoClicarFora, true);
            document.removeEventListener('keydown', aoTeclar, true);
        }
    }

    function aoClicarFora(evento) {
        if (popupAtivo && !popupAtivo.contains(evento.target)) {
            fecharPopup();
        }
    }

    function aoTeclar(evento) {
        if (evento.key === 'Escape') {
            fecharPopup();
        }
    }

    // Posiciona o popup centralizado no botão (empurrando pra dentro da
    // viewport quando necessário) e aponta a seta (::before/::after em CSS)
    // para o centro do botão, mesmo quando o popup precisou ser deslocado.
    function posicionar(popup, elemento) {
        var retanguloLink = elemento.getBoundingClientRect();
        var retanguloPopup = popup.getBoundingClientRect();
        var margemSeta = 14;

        var espacoAbaixo = window.innerHeight - retanguloLink.bottom;
        var ficaAbaixo = espacoAbaixo >= retanguloPopup.height + 10;

        var topo = ficaAbaixo
            ? window.scrollY + retanguloLink.bottom + 10
            : window.scrollY + retanguloLink.top - retanguloPopup.height - 10;

        var centroBotao = window.scrollX + retanguloLink.left + retanguloLink.width / 2;
        var esquerdaIdeal = centroBotao - retanguloPopup.width / 2;
        var esquerdaMin = window.scrollX + 8;
        var esquerdaMax = window.scrollX + document.documentElement.clientWidth - retanguloPopup.width - 8;
        var esquerda = Math.min(Math.max(esquerdaIdeal, esquerdaMin), esquerdaMax);

        popup.style.top = topo + 'px';
        popup.style.left = esquerda + 'px';
        popup.classList.add(ficaAbaixo ? 'box-confirmar-popup--abaixo' : 'box-confirmar-popup--acima');

        var setaEsquerda = centroBotao - esquerda;
        setaEsquerda = Math.min(Math.max(setaEsquerda, margemSeta), retanguloPopup.width - margemSeta);
        popup.style.setProperty('--box-seta', setaEsquerda + 'px');
    }

    // Confirmação inline (popup perto do elemento), sem bloquear a thread
    // como o confirm() nativo. No primeiro clique mostra o popup e devolve
    // false (interrompe a cadeia de onclick do JSF); ao confirmar, marca o
    // elemento e simula um novo clique nele mesmo, que desta vez segue
    // normalmente para a submissão (ajax/action) já configurada pelo JSF.
    window.boxConfirmar = function (elemento, evento, mensagem) {
        if (elemento.dataset.boxConfirmado === 'true') {
            elemento.dataset.boxConfirmado = 'false';
            return true;
        }

        if (evento && evento.preventDefault) {
            evento.preventDefault();
        }
        fecharPopup();

        var popup = document.createElement('div');
        popup.className = 'box-confirmar-popup';

        var texto = document.createElement('p');
        texto.textContent = mensagem;

        var btnSim = document.createElement('button');
        btnSim.type = 'button';
        btnSim.className = 'box-confirmar-sim';
        btnSim.textContent = 'Confirmar';

        var btnNao = document.createElement('button');
        btnNao.type = 'button';
        btnNao.className = 'box-confirmar-nao';
        btnNao.textContent = 'Cancelar';

        popup.appendChild(texto);
        popup.appendChild(btnSim);
        popup.appendChild(btnNao);
        document.body.appendChild(popup);
        popupAtivo = popup;

        posicionar(popup, elemento);

        btnSim.addEventListener('click', function () {
            fecharPopup();
            elemento.dataset.boxConfirmado = 'true';
            elemento.click();
        });
        btnNao.addEventListener('click', fecharPopup);

        window.setTimeout(function () {
            document.addEventListener('click', aoClicarFora, true);
            document.addEventListener('keydown', aoTeclar, true);
        }, 0);

        return false;
    };

    // Alternativa sem nenhum componente/behavior do Faces: qualquer link ou
    // botão com o atributo data-box-confirm já ganha a confirmação, só
    // renderizando o atributo (ex.: p:data-box-confirm="Excluir X?" em cima
    // de um h:commandLink comum). Um único listener delegado no document,
    // na fase de captura — roda ANTES do onclick do próprio elemento (o
    // gerado pelo JSF pra ajax, se houver), então dá pra bloquear com
    // stopImmediatePropagation() no primeiro clique e deixar passar
    // normalmente depois de confirmado, sem precisar de tag/behavior nenhuma
    // do lado do Java. Convive sem conflito com b:confirm: um usa este
    // listener, o outro usa o onclick que o próprio behavior gera; nunca os
    // dois ao mesmo tempo no mesmo elemento.
    document.addEventListener('click', function (evento) {
        var elemento = evento.target.closest('[data-box-confirm]');
        if (!elemento) {
            return;
        }
        var prossegue = window.boxConfirmar(elemento, evento, elemento.getAttribute('data-box-confirm'));
        if (!prossegue) {
            evento.stopImmediatePropagation();
        }
    }, true);
})();
