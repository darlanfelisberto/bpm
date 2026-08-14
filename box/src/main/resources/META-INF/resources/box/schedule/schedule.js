(function () {
    'use strict';

    function readEvents(wrapper) {
        var script = wrapper.querySelector('.box-schedule-events');
        return script ? JSON.parse(script.textContent) : [];
    }

    function initSchedule(wrapper) {
        if (wrapper.dataset.boxScheduleInitialized === 'true') {
            return;
        }
        wrapper.dataset.boxScheduleInitialized = 'true';

        var target = wrapper.querySelector('.box-schedule-calendar');
        var calendar = new window.FullCalendar.Calendar(target, {
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
            events: readEvents(wrapper),
            select: function (info) {
                var data = {};
                data[wrapper.id + '_start'] = info.startStr;
                data[wrapper.id + '_end'] = info.endStr;
                window.Box.trigger(wrapper, 'select', data);
                calendar.unselect();
            },
            eventDrop: function (info) {
                var data = {};
                data[wrapper.id + '_eventId'] = info.event.id;
                data[wrapper.id + '_start'] = info.event.startStr;
                data[wrapper.id + '_end'] = info.event.endStr || info.event.startStr;
                window.Box.trigger(wrapper, 'move', data);
            },
            eventResize: function (info) {
                var data = {};
                data[wrapper.id + '_eventId'] = info.event.id;
                data[wrapper.id + '_start'] = info.event.startStr;
                data[wrapper.id + '_end'] = info.event.endStr || info.event.startStr;
                window.Box.trigger(wrapper, 'resize', data);
            },
            eventClick: function (info) {
                var data = {};
                data[wrapper.id + '_eventId'] = info.event.id;
                window.Box.trigger(wrapper, 'click', data);
            }
        });
        calendar.render();
        wrapper.boxScheduleCalendar = calendar;
    }

    function initAll(root) {
        if (!window.FullCalendar) {
            return;
        }
        (root || document).querySelectorAll('.box-schedule').forEach(initSchedule);
    }

    window.Box.onReadyOrAjax(initAll);
})();
