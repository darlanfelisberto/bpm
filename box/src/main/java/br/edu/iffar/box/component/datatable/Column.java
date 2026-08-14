package br.edu.iffar.box.component.datatable;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * Metadata of a b:datatable column - it does not render anything by
 * itself (the Datatable is what reads its attributes and, row by row,
 * its children).
 *
 * With no children, the cell shows the value of "field" (via default
 * property resolution, like #{var.field}). With children, they define
 * the cell body, evaluated once per row - useful for formatting
 * (e.g.: <b:column header="Status"><span class="badge">#{item.status}
 * </span></b:column>).
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <b:column field="name" header="Name" sortable="true" filterable="true"/>
 */
@FacesComponent(
        value = Column.COMPONENT_TYPE,
        createTag = true,
        tagName = "column",
        namespace = "http://iffar.edu.br/box")
public class Column extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Column";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Column";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /** Name of the property read from the row object (var) and sent to the backend as the sort/filter key. */
    public String getField() {
        return (String) getStateHelper().eval("field");
    }

    public void setField(String field) {
        getStateHelper().put("field", field);
    }

    /** Label shown in the header. With no value, uses "field" itself. */
    public String getHeader() {
        return (String) getStateHelper().eval("header");
    }

    public void setHeader(String header) {
        getStateHelper().put("header", header);
    }

    public boolean isSortable() {
        Boolean sortable = (Boolean) getStateHelper().eval("sortable");
        return sortable != null && sortable;
    }

    public void setSortable(boolean sortable) {
        getStateHelper().put("sortable", sortable);
    }

    public boolean isFilterable() {
        Boolean filterable = (Boolean) getStateHelper().eval("filterable");
        return filterable != null && filterable;
    }

    public void setFilterable(boolean filterable) {
        getStateHelper().put("filterable", filterable);
    }
}
