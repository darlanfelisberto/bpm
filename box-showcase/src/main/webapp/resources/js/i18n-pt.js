// Example of the box i18n extension point (see box-core.js's Box.t/
// Box.messages): the box lib itself only ships English, so a Portuguese
// UI is just this - the app registering its own translations for the
// same keys, in a small JS file included after box-core.js.
//
// Wrapped in DOMContentLoaded (rather than assuming this script tag
// comes after box-core.js's in <head>) so it works regardless of
// resource ordering - by the time DOMContentLoaded fires, every
// synchronous <head> script (including box-core.js) has already run.
document.addEventListener('DOMContentLoaded', function () {
    window.Box.messages.pt = {
        'confirm.yes': 'Confirmar',
        'confirm.no': 'Cancelar',
        'confirm.defaultMessage': 'Tem certeza?',
        'popup.close': 'Fechar',
        'growl.close': 'Fechar',
        'datatable.filterPlaceholder': 'Filtrar…',
        'datatable.filterBy': 'Filtrar por {0}',
        'datatable.noRecords': 'Nenhum registro',
        'datatable.paginationInfo': '{0}–{1} de {2}',
        'autocomplete.noResults': 'Nenhum resultado encontrado',
        'autocomplete.clear': 'Limpar seleção',
        'autocomplete.loading': 'Carregando…'
    };
});
