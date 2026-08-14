(function () {
    'use strict';

    // Unlike schedule2.js, there is no state kept on the client here: every
    // page/sort/filter always hits the server (that is the whole point of
    // lazy loading), so the JS only wires up events on the HTML that
    // Datatable.java already rendered - no rebuilding the DOM by hand.

    function sendPage(wrapper, page) {
        var data = {};
        data[wrapper.id + '_page'] = String(page);
        window.Box.trigger(wrapper, 'page', data);
    }

    function onPaginationButtonClick(event) {
        var button = event.currentTarget;
        var pagination = button.closest('.box-datatable-pagination');
        var wrapper = button.closest('.box-datatable');
        var currentPage = parseInt(pagination.dataset.currentPage, 10) || 0;
        var totalPages = parseInt(pagination.dataset.totalPages, 10) || 1;

        var newPage = currentPage;
        switch (button.dataset.action) {
            case 'first':
                newPage = 0;
                break;
            case 'previous':
                newPage = Math.max(0, currentPage - 1);
                break;
            case 'next':
                newPage = Math.min(totalPages - 1, currentPage + 1);
                break;
            case 'last':
                newPage = totalPages - 1;
                break;
        }
        if (newPage !== currentPage) {
            sendPage(wrapper, newPage);
        }
    }

    // The clicked field is all the server needs - it is the one that
    // decides the next state (none -> asc -> desc -> none), so this rule
    // is not duplicated here (see Datatable.nextSort, tested separately).
    function onSortableHeaderClick(event) {
        var th = event.currentTarget;
        var wrapper = th.closest('.box-datatable');
        var data = {};
        data[wrapper.id + '_sortBy'] = th.dataset.field;
        window.Box.trigger(wrapper, 'sort', data);
    }

    function sendFilters(wrapper) {
        var filters = {};
        wrapper.querySelectorAll('.box-datatable-filter-input').forEach(function (input) {
            filters[input.dataset.field] = input.value;
        });
        var data = {};
        data[wrapper.id + '_filters'] = JSON.stringify(filters);
        window.Box.trigger(wrapper, 'filter', data);
    }

    function onFilterChange(event) {
        sendFilters(event.currentTarget.closest('.box-datatable'));
    }

    function onFilterKeyDown(event) {
        if (event.key === 'Enter') {
            // Without this, Enter inside a text input submits the h:form
            // around it - the filter must be ajax only, never a full page
            // postback.
            event.preventDefault();
            sendFilters(event.currentTarget.closest('.box-datatable'));
        }
    }

    function onRowClick(event) {
        var row = event.currentTarget;
        var wrapper = row.closest('.box-datatable');
        var data = {};
        data[wrapper.id + '_rowId'] = row.dataset.rowId || '';
        window.Box.trigger(wrapper, 'select', data);
    }

    function initDatatable(wrapper) {
        if (wrapper.dataset.boxDatatableInitialized === 'true') {
            return;
        }
        wrapper.dataset.boxDatatableInitialized = 'true';

        wrapper.querySelectorAll('.box-datatable-header-sortable').forEach(function (th) {
            th.addEventListener('click', onSortableHeaderClick);
        });
        wrapper.querySelectorAll('.box-datatable-pagination-button').forEach(function (button) {
            button.addEventListener('click', onPaginationButtonClick);
        });
        wrapper.querySelectorAll('.box-datatable-filter-input').forEach(function (input) {
            input.placeholder = window.Box.t('datatable.filterPlaceholder');
            input.setAttribute('aria-label', window.Box.t('datatable.filterBy', input.dataset.fieldLabel));
            input.addEventListener('change', onFilterChange);
            input.addEventListener('keydown', onFilterKeyDown);
        });
        wrapper.querySelectorAll('.box-datatable-row').forEach(function (row) {
            row.addEventListener('click', onRowClick);
        });

        var info = wrapper.querySelector('.box-datatable-pagination-info');
        if (info) {
            var pagination = info.closest('.box-datatable-pagination');
            var total = parseInt(pagination.dataset.totalRecords, 10) || 0;
            info.textContent = total === 0
                ? window.Box.t('datatable.noRecords')
                : window.Box.t('datatable.paginationInfo',
                    pagination.dataset.firstRecord, pagination.dataset.lastRecord, total);
        }
    }

    function initAll(root) {
        (root || document).querySelectorAll('.box-datatable').forEach(initDatatable);
    }

    window.Box.onReadyOrAjax(initAll);
})();
