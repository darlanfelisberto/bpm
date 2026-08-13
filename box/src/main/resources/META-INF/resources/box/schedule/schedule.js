(function () {
    'use strict';

    function lerEventos(wrapper) {
        var script = wrapper.querySelector('.box-schedule-eventos');
        return script ? JSON.parse(script.textContent) : [];
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
                window.Box.disparar(wrapper, 'select', dados);
                calendar.unselect();
            },
            eventDrop: function (info) {
                var dados = {};
                dados[wrapper.id + '_eventoId'] = info.event.id;
                dados[wrapper.id + '_inicio'] = info.event.startStr;
                dados[wrapper.id + '_fim'] = info.event.endStr || info.event.startStr;
                window.Box.disparar(wrapper, 'move', dados);
            },
            eventResize: function (info) {
                var dados = {};
                dados[wrapper.id + '_eventoId'] = info.event.id;
                dados[wrapper.id + '_inicio'] = info.event.startStr;
                dados[wrapper.id + '_fim'] = info.event.endStr || info.event.startStr;
                window.Box.disparar(wrapper, 'resize', dados);
            },
            eventClick: function (info) {
                var dados = {};
                dados[wrapper.id + '_eventoId'] = info.event.id;
                window.Box.disparar(wrapper, 'click', dados);
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

    window.Box.aoProntoOuAjax(iniciarTodos);
})();
