package br.edu.iffar.box.component.datatable;

import java.util.Map;

/**
 * Parameters of a page requested to {@link DatatableLazyModel}: offset and
 * page size, sort field/direction (null/false = no sorting) and per-column
 * filters (field -> typed text, never null, but can come empty).
 */
public record DatatableQuery(
        int first,
        int size,
        String sortBy,
        boolean sortAscending,
        Map<String, String> filters) {
}
