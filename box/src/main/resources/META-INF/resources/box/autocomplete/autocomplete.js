(function () {
    'use strict';

    var debounceTimers = new WeakMap();

    function showPanel(wrapper) {
        var panel = wrapper.querySelector('.box-autocomplete-panel');
        if (panel) {
            panel.style.display = 'block';
        }
    }

    function hidePanel(wrapper) {
        var panel = wrapper.querySelector('.box-autocomplete-panel');
        if (panel) {
            panel.style.display = 'none';
            var activeItem = panel.querySelector('.box-autocomplete-active');
            if (activeItem) {
                activeItem.classList.remove('box-autocomplete-active');
            }
        }
    }

    function isPanelVisible(wrapper) {
        var panel = wrapper.querySelector('.box-autocomplete-panel');
        return panel && panel.style.display !== 'none';
    }

    function sendQuery(wrapper, queryText) {
        var loader = wrapper.querySelector('.box-autocomplete-loading');
        if (loader) {
            loader.style.display = '';
        }
        wrapper.dataset.boxQuerying = 'true';
        var data = {};
        data[wrapper.id + '_query'] = queryText;
        window.Box.trigger(wrapper, 'query', data);
    }

    function selectItem(wrapper, item) {
        var input = wrapper.querySelector('.box-autocomplete-input');
        var hidden = wrapper.querySelector('.box-autocomplete-value');
        var clearBtn = wrapper.querySelector('.box-autocomplete-clear');
        var value = item.dataset.itemValue || '';
        var label = item.dataset.itemLabel || '';

        input.value = label;
        hidden.value = value;
        wrapper.dataset.selectedLabel = label;
        wrapper.dataset.selectedValue = value;

        if (clearBtn) {
            clearBtn.style.display = label ? '' : 'none';
        }
        hidePanel(wrapper);

        var data = {};
        data[wrapper.id + '_itemSelect'] = value;
        data[wrapper.id] = value;
        window.Box.trigger(wrapper, 'itemSelect', data);
    }

    function highlightItem(panel, direction) {
        var items = panel.querySelectorAll('.box-autocomplete-item');
        if (!items || items.length === 0) {
            return;
        }

        var activeIndex = -1;
        for (var i = 0; i < items.length; i++) {
            if (items[i].classList.contains('box-autocomplete-active')) {
                activeIndex = i;
                break;
            }
        }

        if (activeIndex >= 0) {
            items[activeIndex].classList.remove('box-autocomplete-active');
        }

        var nextIndex = activeIndex + direction;
        if (nextIndex < 0) {
            nextIndex = items.length - 1;
        } else if (nextIndex >= items.length) {
            nextIndex = 0;
        }

        var nextItem = items[nextIndex];
        nextItem.classList.add('box-autocomplete-active');
        nextItem.scrollIntoView({ block: 'nearest' });
    }

    function onInput(event) {
        var input = event.currentTarget;
        var wrapper = input.closest('.box-autocomplete');
        var minQueryLength = parseInt(wrapper.dataset.minQueryLength, 10) || 1;
        var queryDelay = parseInt(wrapper.dataset.queryDelay, 10) || 300;
        var clearBtn = wrapper.querySelector('.box-autocomplete-clear');

        if (clearBtn) {
            clearBtn.style.display = input.value ? '' : 'none';
        }

        var forceSelection = wrapper.dataset.forceSelection === 'true';
        if (!forceSelection) {
            var hidden = wrapper.querySelector('.box-autocomplete-value');
            if (hidden) {
                hidden.value = input.value;
            }
        }

        var timer = debounceTimers.get(wrapper);
        if (timer) {
            clearTimeout(timer);
        }

        if (input.value.length < minQueryLength) {
            hidePanel(wrapper);
            return;
        }

        debounceTimers.set(wrapper, setTimeout(function () {
            sendQuery(wrapper, input.value);
        }, queryDelay));
    }

    function onKeyDown(event) {
        var input = event.currentTarget;
        var wrapper = input.closest('.box-autocomplete');
        var panel = wrapper.querySelector('.box-autocomplete-panel');
        var isVisible = isPanelVisible(wrapper);

        switch (event.key) {
            case 'ArrowDown':
                event.preventDefault();
                if (!isVisible) {
                    if (panel && panel.querySelector('.box-autocomplete-item, .box-autocomplete-empty')) {
                        showPanel(wrapper);
                    } else {
                        sendQuery(wrapper, input.value);
                    }
                } else {
                    highlightItem(panel, 1);
                }
                break;

            case 'ArrowUp':
                if (isVisible) {
                    event.preventDefault();
                    highlightItem(panel, -1);
                }
                break;

            case 'Enter':
                if (isVisible) {
                    var activeItem = panel.querySelector('.box-autocomplete-item.box-autocomplete-active');
                    if (activeItem) {
                        event.preventDefault();
                        selectItem(wrapper, activeItem);
                    }
                }
                break;

            case 'Escape':
                if (isVisible) {
                    event.preventDefault();
                    hidePanel(wrapper);
                }
                break;

            case 'Tab':
                if (isVisible) {
                    var currentActive = panel.querySelector('.box-autocomplete-item.box-autocomplete-active');
                    if (currentActive) {
                        selectItem(wrapper, currentActive);
                    } else {
                        hidePanel(wrapper);
                    }
                }
                break;
        }
    }

    function onBlur(event) {
        var input = event.currentTarget;
        var wrapper = input.closest('.box-autocomplete');
        var forceSelection = wrapper.dataset.forceSelection === 'true';

        setTimeout(function () {
            if (forceSelection) {
                var selectedLabel = wrapper.dataset.selectedLabel || '';
                var selectedValue = wrapper.dataset.selectedValue || '';
                if (input.value !== selectedLabel) {
                    input.value = selectedLabel;
                    var hidden = wrapper.querySelector('.box-autocomplete-value');
                    if (hidden) {
                        hidden.value = selectedValue;
                    }
                    var clearBtn = wrapper.querySelector('.box-autocomplete-clear');
                    if (clearBtn) {
                        clearBtn.style.display = selectedLabel ? '' : 'none';
                    }
                }
            }
            hidePanel(wrapper);
        }, 200);
    }

    function onDropdownClick(event) {
        var button = event.currentTarget;
        var wrapper = button.closest('.box-autocomplete');
        var input = wrapper.querySelector('.box-autocomplete-input');
        var dropdownMode = wrapper.dataset.dropdownMode || 'blank';

        input.focus();
        if (isPanelVisible(wrapper)) {
            hidePanel(wrapper);
        } else {
            var query = dropdownMode === 'current' ? input.value : '';
            sendQuery(wrapper, query);
        }
    }

    function onClearClick(event) {
        var button = event.currentTarget;
        var wrapper = button.closest('.box-autocomplete');
        var input = wrapper.querySelector('.box-autocomplete-input');
        var hidden = wrapper.querySelector('.box-autocomplete-value');

        input.value = '';
        if (hidden) {
            hidden.value = '';
        }
        wrapper.dataset.selectedLabel = '';
        wrapper.dataset.selectedValue = '';
        button.style.display = 'none';
        hidePanel(wrapper);
        input.focus();

        var data = {};
        data[wrapper.id + '_value'] = '';
        data[wrapper.id] = '';
        window.Box.trigger(wrapper, 'clear', data);
    }

    function onPanelClick(event) {
        var item = event.target.closest('.box-autocomplete-item');
        if (item) {
            var wrapper = item.closest('.box-autocomplete');
            selectItem(wrapper, item);
        }
    }

    function initAutocomplete(wrapper) {
        var input = wrapper.querySelector('.box-autocomplete-input');
        var panel = wrapper.querySelector('.box-autocomplete-panel');
        var dropdownBtn = wrapper.querySelector('.box-autocomplete-dropdown');
        var clearBtn = wrapper.querySelector('.box-autocomplete-clear');
        var loader = wrapper.querySelector('.box-autocomplete-loading');

        // Any element marked data-box-i18n gets its text (or, with
        // data-box-i18n-attr, the named attribute - e.g. aria-label on the
        // clear/loading buttons, which have no visible text of their own)
        // resolved from Box.t. Runs on every (re)render, including partial
        // ajax updates that replace the results panel.
        wrapper.querySelectorAll('[data-box-i18n]').forEach(function (el) {
            var text = window.Box.t(el.dataset.boxI18n);
            var attr = el.dataset.boxI18nAttr;
            if (attr) {
                el.setAttribute(attr, text);
            } else {
                el.textContent = text;
            }
        });

        // If this component just returned from an ajax query
        if (wrapper.dataset.boxQuerying === 'true') {
            delete wrapper.dataset.boxQuerying;
            if (loader) {
                loader.style.display = 'none';
            }
            if (panel && (panel.querySelector('.box-autocomplete-list') || panel.querySelector('.box-autocomplete-empty'))) {
                showPanel(wrapper);
                if (input) {
                    input.focus();
                }
            }
        }

        if (wrapper.dataset.boxAutocompleteInitialized === 'true') {
            return;
        }
        wrapper.dataset.boxAutocompleteInitialized = 'true';

        if (input) {
            input.addEventListener('input', onInput);
            input.addEventListener('keydown', onKeyDown);
            input.addEventListener('blur', onBlur);
        }

        if (dropdownBtn) {
            dropdownBtn.addEventListener('click', onDropdownClick);
        }

        if (clearBtn) {
            clearBtn.addEventListener('click', onClearClick);
        }

        if (panel) {
            panel.addEventListener('mousedown', function (e) {
                // Prevent input blur before click finishes
                e.preventDefault();
            });
            panel.addEventListener('click', onPanelClick);
        }
    }

    function initAll(root) {
        (root || document).querySelectorAll('.box-autocomplete').forEach(initAutocomplete);
    }

    document.addEventListener('click', function (event) {
        document.querySelectorAll('.box-autocomplete').forEach(function (wrapper) {
            if (!wrapper.contains(event.target)) {
                hidePanel(wrapper);
            }
        });
    });

    window.Box.onReadyOrAjax(initAll);
})();
