(function () {
    'use strict';

    // Clique no backdrop nativo do <dialog> (a pseudo-elemento ::backdrop)
    // borbulha com o proprio <dialog> como target - e o unico jeito de
    // detectar "clicou fora" sem medir coordenadas.
    function aoClicarNoBackdrop(evento) {
        var dialog = evento.currentTarget;
        if (evento.target === dialog && dialog.dataset.boxPopupFechavel !== 'false') {
            dialog.close();
        }
    }

    // Esc dispara "cancel" antes de fechar - previne quando closable=false.
    function aoCancelar(evento) {
        if (evento.target.dataset.boxPopupFechavel === 'false') {
            evento.preventDefault();
        }
    }

    function iniciarPopup(dialog) {
        if (dialog.dataset.boxPopupIniciado === 'true') {
            return;
        }
        dialog.dataset.boxPopupIniciado = 'true';

        dialog.addEventListener('click', aoClicarNoBackdrop);
        dialog.addEventListener('cancel', aoCancelar);

        var botaoFechar = dialog.querySelector('.box-popup-fechar');
        if (botaoFechar) {
            botaoFechar.addEventListener('click', function () {
                dialog.close();
            });
        }
    }

    function iniciarTodos(raiz) {
        (raiz || document).querySelectorAll('.box-popup').forEach(iniciarPopup);
    }

    window.Box.popup = {
        abrir: function (id) {
            var dialog = document.getElementById(id);
            if (dialog) {
                dialog.showModal();
            }
        },
        fechar: function (id) {
            var dialog = document.getElementById(id);
            if (dialog) {
                dialog.close();
            }
        }
    };

    window.Box.aoProntoOuAjax(iniciarTodos);
})();
