package br.edu.iffar.box.component.datatable;

import java.util.List;

/** A page of results: the requested rows and the total record count (ignoring the page slice, only the filter/search). */
public record DatatablePage<T>(List<T> rows, long total) {
}
