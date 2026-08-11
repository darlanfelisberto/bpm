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

    function posicionar(popup, elemento) {
        var retanguloLink = elemento.getBoundingClientRect();
        var retanguloPopup = popup.getBoundingClientRect();
        var espacoAbaixo = window.innerHeight - retanguloLink.bottom;

        var topo = espacoAbaixo >= retanguloPopup.height + 8
            ? window.scrollY + retanguloLink.bottom + 6
            : window.scrollY + retanguloLink.top - retanguloPopup.height - 6;

        var esquerda = Math.min(
            window.scrollX + retanguloLink.left,
            window.scrollX + document.documentElement.clientWidth - retanguloPopup.width - 8
        );

        popup.style.top = topo + 'px';
        popup.style.left = Math.max(8, esquerda) + 'px';
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
})();
