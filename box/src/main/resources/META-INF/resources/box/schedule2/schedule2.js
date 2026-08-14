(function () {
    'use strict';

    // Same as schedule.js (b:schedule), but with no FullCalendar: the
    // month grid, navigation between months and drag-and-drop are all
    // hand-written here. Month view only (no week/day, no resizing -
    // would require an hour grid, the most laborious part to reproduce).
    // Multi-day: the same event appears repeated on each day it touches
    // (no continuous bar like FullCalendar draws).

    function readEvents(wrapper) {
        var script = wrapper.querySelector('.box-schedule2-events');
        return script ? JSON.parse(script.textContent) : [];
    }

    function twoDigits(n) {
        return n < 10 ? '0' + n : '' + n;
    }

    function formatDate(date) {
        return date.getFullYear() + '-' + twoDigits(date.getMonth() + 1) + '-' + twoDigits(date.getDate());
    }

    function formatDateTime(date) {
        return formatDate(date) + 'T' + twoDigits(date.getHours()) + ':'
                + twoDigits(date.getMinutes()) + ':' + twoDigits(date.getSeconds());
    }

    function dateOnly(date) {
        return new Date(date.getFullYear(), date.getMonth(), date.getDate());
    }

    function addDays(date, days) {
        var copy = new Date(date);
        copy.setDate(copy.getDate() + days);
        return copy;
    }

    // Full weeks (Sun-Sat) that cover the entire month - includes days
    // from the previous/next month to fill out the first/last week.
    function generateWeeks(year, month) {
        var firstOfMonth = new Date(year, month, 1);
        var cursor = addDays(firstOfMonth, -firstOfMonth.getDay());
        var lastOfMonth = new Date(year, month + 1, 0);
        var weeks = [];
        do {
            var week = [];
            for (var i = 0; i < 7; i++) {
                week.push(new Date(cursor));
                cursor = addDays(cursor, 1);
            }
            weeks.push(week);
        } while (cursor <= lastOfMonth);
        return weeks;
    }

    // One Schedule2Grid per .box-schedule2 wrapper - holds the month/year
    // currently being shown and the events, and knows how to (re)draw the
    // grid from that state.
    class Schedule2Grid {
        constructor(wrapper) {
            this.wrapper = wrapper;
            this.events = readEvents(wrapper);
            var today = new Date();
            this.year = today.getFullYear();
            this.month = today.getMonth();
        }

        eventsOnDay(day) {
            return this.events.filter(function (event) {
                if (!event.start) {
                    return false;
                }
                var start = dateOnly(new Date(event.start));
                var end = event.end ? dateOnly(new Date(event.end)) : start;
                return day >= start && day <= end;
            });
        }

        moveEvent(eventId, newDay) {
            var event = this.events.find(function (e) { return e.id === eventId; });
            if (!event) {
                return;
            }
            var originalStart = new Date(event.start);
            var dayDifference = Math.round((newDay - dateOnly(originalStart)) / 86400000);
            if (dayDifference === 0) {
                return;
            }
            var newStart = addDays(originalStart, dayDifference);
            event.start = formatDateTime(newStart);
            var newEndText = event.start;
            if (event.end) {
                var newEnd = addDays(new Date(event.end), dayDifference);
                event.end = formatDateTime(newEnd);
                newEndText = event.end;
            }

            this.renderGrid();

            var data = {};
            data[this.wrapper.id + '_eventId'] = eventId;
            data[this.wrapper.id + '_start'] = event.start;
            data[this.wrapper.id + '_end'] = newEndText;
            window.Box.trigger(this.wrapper, 'move', data);
        }

        createCell(day) {
            var self = this;
            var cell = document.createElement('div');
            cell.className = 'box-schedule2-day';
            if (day.getMonth() !== this.month) {
                cell.classList.add('box-schedule2-day-outside');
            }
            var today = dateOnly(new Date());
            if (day.getTime() === today.getTime()) {
                cell.classList.add('box-schedule2-day-today');
            }

            var number = document.createElement('span');
            number.className = 'box-schedule2-day-number';
            number.textContent = day.getDate();
            cell.appendChild(number);

            this.eventsOnDay(day).forEach(function (event) {
                var chip = document.createElement('div');
                chip.className = 'box-schedule2-event';
                chip.textContent = event.title;
                chip.draggable = true;
                if (event.color) {
                    chip.style.backgroundColor = event.color;
                }
                chip.addEventListener('dragstart', function (dragEvent) {
                    dragEvent.dataTransfer.setData('text/plain', event.id);
                    dragEvent.dataTransfer.effectAllowed = 'move';
                });
                chip.addEventListener('click', function (clickEvent) {
                    clickEvent.stopPropagation();
                    var data = {};
                    data[self.wrapper.id + '_eventId'] = event.id;
                    window.Box.trigger(self.wrapper, 'click', data);
                });
                cell.appendChild(chip);
            });

            cell.addEventListener('dragover', function (dragEvent) {
                dragEvent.preventDefault();
                dragEvent.dataTransfer.dropEffect = 'move';
            });
            cell.addEventListener('drop', function (dropEvent) {
                dropEvent.preventDefault();
                var eventId = dropEvent.dataTransfer.getData('text/plain');
                self.moveEvent(eventId, day);
            });
            cell.addEventListener('click', function () {
                var data = {};
                data[self.wrapper.id + '_start'] = formatDate(day);
                data[self.wrapper.id + '_end'] = formatDate(addDays(day, 1));
                window.Box.trigger(self.wrapper, 'select', data);
            });

            return cell;
        }

        renderGrid() {
            var self = this;
            var target = this.wrapper.querySelector('.box-schedule2-calendar');
            target.replaceChildren();

            var header = document.createElement('div');
            header.className = 'box-schedule2-header';

            var previousButton = document.createElement('button');
            previousButton.type = 'button';
            previousButton.textContent = '‹';
            previousButton.addEventListener('click', function () {
                self.month -= 1;
                if (self.month < 0) {
                    self.month = 11;
                    self.year -= 1;
                }
                self.renderGrid();
            });

            var nextButton = document.createElement('button');
            nextButton.type = 'button';
            nextButton.textContent = '›';
            nextButton.addEventListener('click', function () {
                self.month += 1;
                if (self.month > 11) {
                    self.month = 0;
                    self.year += 1;
                }
                self.renderGrid();
            });

            var title = document.createElement('span');
            title.className = 'box-schedule2-title';
            var titleText = new Date(this.year, this.month, 1)
                    .toLocaleDateString('pt-BR', {month: 'long', year: 'numeric'});
            // only the first letter uppercase ("Agosto de 2026") -
            // text-transform: capitalize would uppercase every word ("Agosto De 2026").
            title.textContent = titleText.charAt(0).toUpperCase() + titleText.slice(1);

            header.appendChild(previousButton);
            header.appendChild(title);
            header.appendChild(nextButton);
            target.appendChild(header);

            var grid = document.createElement('div');
            grid.className = 'box-schedule2-grid';

            var weekdayFormatter = new Intl.DateTimeFormat('pt-BR', {weekday: 'short'});
            for (var i = 0; i < 7; i++) {
                var headerDay = document.createElement('div');
                headerDay.className = 'box-schedule2-header-day';
                headerDay.textContent = weekdayFormatter.format(new Date(2026, 0, 4 + i));
                grid.appendChild(headerDay);
            }

            generateWeeks(this.year, this.month).forEach(function (week) {
                week.forEach(function (day) {
                    grid.appendChild(self.createCell(day));
                });
            });

            target.appendChild(grid);
        }
    }

    function initSchedule2(wrapper) {
        if (wrapper.dataset.boxSchedule2Initialized === 'true') {
            return;
        }
        wrapper.dataset.boxSchedule2Initialized = 'true';

        new Schedule2Grid(wrapper).renderGrid();
    }

    function initAll(root) {
        (root || document).querySelectorAll('.box-schedule2').forEach(initSchedule2);
    }

    window.Box.onReadyOrAjax(initAll);
})();
