(function () {
    'use strict';

    function lerEventos(wrapper) {
        var script = wrapper.querySelector('.box-schedule-eventos');
        return script ? JSON.parse(script.textContent) : [];
    }

    // Dispara um client behavior (select/move/resize/click) via a mesma
    // API que o proprio f:ajax usa por baixo - "jakarta.faces.behavior.event"
    // é o nome do evento que Schedule.decode() confere pra saber qual
    // f:ajax aninhado despachar.
    function disparar(wrapper, nomeEvento, dados) {
        var opcoes = Object.assign({
            execute: '@this',
            render: wrapper.dataset['render' + nomeEvento[0].toUpperCase() + nomeEvento.slice(1)] || '@none',
            'jakarta.faces.behavior.event': nomeEvento
        }, dados);
        window.faces.ajax.request(wrapper.id, null, opcoes);
    }

    function iniciarSchedule(wrapper) {
        if (wrapper.dataset.boxScheduleIniciado === 'true') {
            return;
        }
        wrapper.dataset.boxScheduleIniciado = 'true';

        var alvo = wrapper.querySelector('.box-schedule-calendario');
        var calendar = new window.FullCalendar.Calendar(alvo, {
            locale: 'pt-br',
            height: 'auto',
            initialView: 'dayGridMonth',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay'
            },
            selectable: true,
            editable: true,
            events: lerEventos(wrapper),
            select: function (info) {
                var dados = {};
                dados[wrapper.id + '_inicio'] = info.startStr;
                dados[wrapper.id + '_fim'] = info.endStr;
                disparar(wrapper, 'select', dados);
                calendar.unselect();
            },
            eventDrop: function (info) {
                var dados = {};
                dados[wrapper.id + '_eventoId'] = info.event.id;
                dados[wrapper.id + '_inicio'] = info.event.startStr;
                dados[wrapper.id + '_fim'] = info.event.endStr || info.event.startStr;
                disparar(wrapper, 'move', dados);
            },
            eventResize: function (info) {
                var dados = {};
                dados[wrapper.id + '_eventoId'] = info.event.id;
                dados[wrapper.id + '_inicio'] = info.event.startStr;
                dados[wrapper.id + '_fim'] = info.event.endStr || info.event.startStr;
                disparar(wrapper, 'resize', dados);
            },
            eventClick: function (info) {
                var dados = {};
                dados[wrapper.id + '_eventoId'] = info.event.id;
                disparar(wrapper, 'click', dados);
            }
        });
        calendar.render();
        wrapper.boxScheduleCalendar = calendar;
    }

    function iniciarTodos(raiz) {
        if (!window.FullCalendar) {
            return;
        }
        (raiz || document).querySelectorAll('.box-schedule').forEach(iniciarSchedule);
    }

    document.addEventListener('DOMContentLoaded', function () {
        iniciarTodos(document);

        // Ver editor.js pro motivo desta checagem ficar aqui dentro (nao
        // solta no fim do arquivo): faces.js e incluido DEPOIS de
        // schedule.js na pagina.
        if (window.faces && window.faces.ajax && window.faces.ajax.addOnEvent) {
            window.faces.ajax.addOnEvent(function (dados) {
                if (dados.status === 'success') {
                    iniciarTodos(document);
                }
            });
        }
    });
})();
