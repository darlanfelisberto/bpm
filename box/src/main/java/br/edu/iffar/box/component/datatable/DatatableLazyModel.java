package br.edu.iffar.box.component.datatable;

/**
 * Data source of b:datatable. Every interaction (paginate, sort, filter)
 * calls {@link #load(DatatableQuery)} again - no row stays kept in the
 * view state between requests, only the query parameters
 * (page/sort/filters), so the implementation is responsible for fetching
 * (database, service, etc.) only the requested slice.
 */
public interface DatatableLazyModel<T> {

    DatatablePage<T> load(DatatableQuery query);

    /** Used when the component does not receive the "pageSize" attribute. */
    default int defaultPageSize() {
        return 10;
    }
}
