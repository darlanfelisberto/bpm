(function () {
    'use strict';

    function initMenu(root) {
        var context = root || document;
        var headers = context.querySelectorAll('.box-menu-collapsible > .box-menu-submenu-header');
        headers.forEach(function (header) {
            if (header.dataset.boxMenuInit) {
                return;
            }
            header.dataset.boxMenuInit = 'true';

            function toggleSubmenu() {
                var submenu = header.closest('.box-menu-submenu');
                if (!submenu) {
                    return;
                }
                var isCollapsed = submenu.classList.toggle('box-menu-collapsed');
                header.setAttribute('aria-expanded', String(!isCollapsed));
            }

            header.addEventListener('click', function () {
                toggleSubmenu();
            });

            header.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' || e.key === ' ' || e.key === 'Spacebar') {
                    e.preventDefault();
                    toggleSubmenu();
                }
            });
        });
    }

    if (window.Box && window.Box.onReadyOrAjax) {
        window.Box.onReadyOrAjax(initMenu);
    } else {
        document.addEventListener('DOMContentLoaded', function () {
            initMenu(document);
        });
    }
})();
