package br.edu.iffar.box.component.datatable;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Table with pagination/sorting/filtering always resolved on the backend -
 * every interaction (clicking a page, a sortable header, typing in a
 * filter) triggers a postback that calls
 * {@link DatatableLazyModel#load(DatatableQuery)} again. No row stays kept
 * between requests: only control primitives (page, sort field/direction,
 * filters) go into the view state.
 *
 * Client behaviors "page", "sort" and "filter" (always auto-render the
 * table itself, even without an explicit &lt;f:ajax&gt;, because without
 * that the table would never show the result of the interaction) and
 * "select" (row click - default event, like b:schedule2 - informational,
 * does not auto-render anything by default).
 *
 * Row selection does not keep the object: only the id (via a Converter)
 * travels in the request, like the transient "eventId" field of
 * b:schedule2. A convenience getter (getSelectedObject) uses the Converter
 * to rebuild the object on demand, without anything staying in the
 * StateHelper.
 *
 * Optional facets "header"/"footer" (block above/below the table, e.g.
 * title, action bar) and "empty" (replaces the table body when the
 * current page has no row at all - usually because of a filter).
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <b:datatable value="#{bean.model}" var="item" pageSize="20"
 *                    converter="#{bean.idConverter}">
 *          <f:facet name="header">Table title</f:facet>
 *          <b:column field="name" header="Name" sortable="true" filterable="true"/>
 *          <b:column field="email" header="Email" filterable="true"/>
 *          <b:column header="Status">
 *              <span class="badge">#{item.status}</span>
 *          </b:column>
 *          <f:facet name="empty">No records found.</f:facet>
 *          <f:ajax event="select" listener="#{bean.onSelect}" render=":result"/>
 *      </b:datatable>
 */
@FacesComponent(
        value = Datatable.COMPONENT_TYPE,
        createTag = true,
        tagName = "datatable",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "datatable/datatable.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "datatable/datatable.js", target = "head")
})
public class Datatable extends UIComponentBase implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Datatable";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Datatable";

    private static final List<String> EVENT_NAMES =
            Collections.unmodifiableList(List.of("page", "sort", "filter", "select"));

    private final Map<String, List<ClientBehavior>> behaviors = new HashMap<>();

    private transient String rowIdData;

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public Map<String, List<ClientBehavior>> getClientBehaviors() {
        return Collections.unmodifiableMap(behaviors);
    }

    @Override
    public void addClientBehavior(String eventName, ClientBehavior behavior) {
        behaviors.computeIfAbsent(eventName, k -> new ArrayList<>()).add(behavior);
    }

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return "select";
    }

    @SuppressWarnings("unchecked")
    public DatatableLazyModel<Object> getValue() {
        return (DatatableLazyModel<Object>) getStateHelper().eval("value");
    }

    public void setValue(DatatableLazyModel<Object> value) {
        getStateHelper().put("value", value);
    }

    /** Name that exposes the current row in EL to the columns' children (like h:dataTable's "var"). */
    public String getVar() {
        return (String) getStateHelper().eval("var");
    }

    public void setVar(String var) {
        getStateHelper().put("var", var);
    }

    /** Optional - with no value (or <= 0), uses {@link DatatableLazyModel#defaultPageSize()}. */
    public Integer getPageSize() {
        return (Integer) getStateHelper().eval("pageSize");
    }

    public void setPageSize(Integer pageSize) {
        getStateHelper().put("pageSize", pageSize);
    }

    /** Needed only for the "select" event: converts the clicked row to an id (there) and back to the object (return). */
    public Converter getConverter() {
        return (Converter) getStateHelper().eval("converter");
    }

    public void setConverter(Converter converter) {
        getStateHelper().put("converter", converter);
    }

    public String getDefaultSortBy() {
        return (String) getStateHelper().eval("defaultSortBy");
    }

    public void setDefaultSortBy(String defaultSortBy) {
        getStateHelper().put("defaultSortBy", defaultSortBy);
    }

    public boolean isDefaultSortAscending() {
        Boolean value = (Boolean) getStateHelper().eval("defaultSortAscending");
        return value == null || value;
    }

    public void setDefaultSortAscending(boolean defaultSortAscending) {
        getStateHelper().put("defaultSortAscending", defaultSortAscending);
    }

    /** Id (via Converter) of the row clicked in the "select" event - null in the other events. */
    public String getRowId() {
        return rowIdData;
    }

    /** Rebuilds the selected row from the id, via Converter - null with no selection or no Converter configured. */
    public Object getSelectedObject() {
        if (rowIdData == null) {
            return null;
        }
        Converter converter = getConverter();
        if (converter == null) {
            return null;
        }
        return converter.getAsObject(FacesContext.getCurrentInstance(), this, rowIdData);
    }

    private int currentPage() {
        Integer page = (Integer) getStateHelper().get("page");
        return page != null ? page : 0;
    }

    private String currentSortBy() {
        String value = (String) getStateHelper().get("sortBy");
        return value != null ? value : getDefaultSortBy();
    }

    private boolean currentSortAscending() {
        Boolean value = (Boolean) getStateHelper().get("sortAscending");
        return value != null ? value : isDefaultSortAscending();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> currentFilters() {
        Map<String, String> filters = (Map<String, String>) getStateHelper().get("filters");
        return filters != null ? filters : Map.of();
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();

        if (!clientId.equals(parameters.get("jakarta.faces.source"))) {
            return;
        }
        String eventName = parameters.get("jakarta.faces.behavior.event");
        if (eventName == null) {
            return;
        }

        switch (eventName) {
            case "page" -> decodePage(parameters, clientId);
            case "sort" -> decodeSort(parameters, clientId);
            case "filter" -> decodeFilters(parameters, clientId);
            case "select" -> rowIdData = parameters.get(clientId + "_rowId");
            default -> { }
        }

        List<ClientBehavior> eventBehaviors = behaviors.get(eventName);
        if (eventBehaviors != null) {
            for (ClientBehavior behavior : eventBehaviors) {
                behavior.decode(context, this);
            }
        }
    }

    private void decodePage(Map<String, String> parameters, String clientId) {
        Integer page = parseInt(parameters.get(clientId + "_page"));
        if (page != null) {
            getStateHelper().put("page", Math.max(0, page));
        }
    }

    private void decodeSort(Map<String, String> parameters, String clientId) {
        String clickedField = parameters.get(clientId + "_sortBy");
        if (clickedField == null || clickedField.isBlank()) {
            return;
        }
        Sort next = nextSort(clickedField, currentSortBy(), currentSortAscending());
        getStateHelper().put("sortBy", next.field());
        getStateHelper().put("sortAscending", next.ascending());
        getStateHelper().put("page", 0);
    }

    private void decodeFilters(Map<String, String> parameters, String clientId) {
        getStateHelper().put("filters", new HashMap<>(parseFilters(parameters.get(clientId + "_filters"))));
        getStateHelper().put("page", 0);
    }

    static Integer parseInt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException error) {
            return null;
        }
    }

    /** None->asc->desc->none cycle when always clicking the same field; clicking another field always goes back to asc. */
    static Sort nextSort(String clickedField, String currentSortBy, boolean currentAscending) {
        if (!clickedField.equals(currentSortBy)) {
            return new Sort(clickedField, true);
        }
        if (currentAscending) {
            return new Sort(clickedField, false);
        }
        return new Sort(null, true);
    }

    /** Malformed/tampered JSON (field->text) becomes "no filter at all" instead of failing the request. */
    static Map<String, String> parseFilters(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        Map<String, String> filters = new LinkedHashMap<>();
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject object = reader.readObject();
            for (String field : object.keySet()) {
                String value = object.getString(field, "");
                if (!value.isBlank()) {
                    filters.put(field, value);
                }
            }
        } catch (RuntimeException error) {
            return Map.of();
        }
        return filters;
    }

    /** Fixes a page that ended up out of range (e.g. a filter reduced the total) to the last valid page. */
    static int clampPageToBounds(int page, int pageSize, long total) {
        if (pageSize <= 0 || total <= 0) {
            return 0;
        }
        long totalPages = (total + pageSize - 1) / pageSize;
        long first = (long) page * pageSize;
        if (first < total) {
            return page;
        }
        return (int) Math.max(0, totalPages - 1);
    }

    record Sort(String field, boolean ascending) {
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        String clientId = getClientId(context);

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", "box-datatable", null);
        for (String eventName : EVENT_NAMES) {
            writer.writeAttribute("data-render-" + eventName, renderTargetFor(eventName, clientId), null);
        }
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        List<Column> columns = columns();
        DatatableLazyModel<Object> model = getValue();
        if (model == null || columns.isEmpty()) {
            return;
        }

        int pageSize = effectivePageSize(model);
        int page = currentPage();
        String sortBy = currentSortBy();
        boolean sortAscending = currentSortAscending();
        Map<String, String> filters = currentFilters();

        DatatablePage<Object> result = model.load(
                new DatatableQuery(page * pageSize, pageSize, sortBy, sortAscending, filters));

        long first = (long) page * pageSize;
        if (result.total() > 0 && first >= result.total()) {
            page = clampPageToBounds(page, pageSize, result.total());
            getStateHelper().put("page", page);
            result = model.load(
                    new DatatableQuery(page * pageSize, pageSize, sortBy, sortAscending, filters));
        }

        ResponseWriter writer = context.getResponseWriter();
        writeFacet(context, writer, "header", "box-datatable-top");
        writeTable(context, writer, columns, result, sortBy, sortAscending, filters);
        writePagination(writer, page, pageSize, result.total());
        writeFacet(context, writer, "footer", "box-datatable-footer");
    }

    private void writeFacet(FacesContext context, ResponseWriter writer, String name, String cssClass) throws IOException {
        UIComponent facet = getFacet(name);
        if (facet == null || !facet.isRendered()) {
            return;
        }
        writer.startElement("div", this);
        writer.writeAttribute("class", cssClass, null);
        facet.encodeAll(context);
        writer.endElement("div");
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div");
    }

    private List<Column> columns() {
        List<Column> columns = new ArrayList<>();
        for (UIComponent child : getChildren()) {
            if (child instanceof Column column && child.isRendered()) {
                columns.add(column);
            }
        }
        return columns;
    }

    private int effectivePageSize(DatatableLazyModel<Object> model) {
        Integer attribute = getPageSize();
        if (attribute != null && attribute > 0) {
            return attribute;
        }
        return model.defaultPageSize();
    }

    private void writeTable(FacesContext context, ResponseWriter writer, List<Column> columns,
            DatatablePage<Object> result, String sortBy, boolean sortAscending, Map<String, String> filters)
            throws IOException {

        boolean anyFilterable = columns.stream().anyMatch(Column::isFilterable);

        writer.startElement("table", this);
        writer.writeAttribute("class", "box-datatable-table", null);

        writer.startElement("thead", this);
        writer.startElement("tr", this);
        writer.writeAttribute("class", "box-datatable-header", null);
        for (Column column : columns) {
            writer.startElement("th", this);
            String field = column.getField();
            if (column.isSortable() && field != null) {
                writer.writeAttribute("class", "box-datatable-header-sortable", null);
                writer.writeAttribute("data-field", field, null);
                if (field.equals(sortBy)) {
                    writer.writeAttribute("data-sort-direction", sortAscending ? "asc" : "desc", null);
                }
            }
            String label = column.getHeader() != null ? column.getHeader() : field;
            writer.writeText(label != null ? label : "", "header");
            if (column.isSortable() && field != null && field.equals(sortBy)) {
                writer.startElement("span", this);
                writer.writeAttribute("class", "box-datatable-sort-indicator", null);
                writer.writeText(sortAscending ? " ▲" : " ▼", null);
                writer.endElement("span");
            }
            writer.endElement("th");
        }
        writer.endElement("tr");

        if (anyFilterable) {
            writer.startElement("tr", this);
            writer.writeAttribute("class", "box-datatable-filters", null);
            for (Column column : columns) {
                writer.startElement("td", this);
                String field = column.getField();
                if (column.isFilterable() && field != null) {
                    String filterLabel = column.getHeader() != null ? column.getHeader() : field;
                    writer.startElement("input", this);
                    writer.writeAttribute("type", "text", null);
                    writer.writeAttribute("class", "box-datatable-filter-input", null);
                    writer.writeAttribute("data-field", field, null);
                    writer.writeAttribute("data-field-label", filterLabel, null);
                    String currentValue = filters.get(field);
                    if (currentValue != null) {
                        writer.writeAttribute("value", currentValue, null);
                    }
                    writer.endElement("input");
                }
                writer.endElement("td");
            }
            writer.endElement("tr");
        }
        writer.endElement("thead");

        writer.startElement("tbody", this);
        String var = getVar();
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        boolean hadPreviousValue = var != null && requestMap.containsKey(var);
        Object previousValue = hadPreviousValue ? requestMap.get(var) : null;
        Converter converter = getConverter();

        if (result.rows().isEmpty()) {
            writeEmptyRow(context, writer, columns.size());
        }
        for (Object row : result.rows()) {
            if (var != null) {
                requestMap.put(var, row);
            }
            writer.startElement("tr", this);
            writer.writeAttribute("class", "box-datatable-row", null);
            if (converter != null) {
                writer.writeAttribute("data-row-id", converter.getAsString(context, this, row), null);
            }
            for (Column column : columns) {
                writer.startElement("td", this);
                if (column.getChildCount() > 0) {
                    for (UIComponent child : column.getChildren()) {
                        child.encodeAll(context);
                    }
                } else {
                    String field = column.getField();
                    Object value = field != null ? fieldValue(context, row, field) : null;
                    writer.writeText(value != null ? value.toString() : "", null);
                }
                writer.endElement("td");
            }
            writer.endElement("tr");
        }

        if (var != null) {
            if (hadPreviousValue) {
                requestMap.put(var, previousValue);
            } else {
                requestMap.remove(var);
            }
        }

        writer.endElement("tbody");
        writer.endElement("table");
    }

    private void writeEmptyRow(FacesContext context, ResponseWriter writer, int colspan) throws IOException {
        UIComponent empty = getFacet("empty");
        if (empty == null || !empty.isRendered()) {
            return;
        }
        writer.startElement("tr", this);
        writer.writeAttribute("class", "box-datatable-empty-row", null);
        writer.startElement("td", this);
        writer.writeAttribute("colspan", String.valueOf(colspan), null);
        empty.encodeAll(context);
        writer.endElement("td");
        writer.endElement("tr");
    }

    private Object fieldValue(FacesContext context, Object row, String field) {
        if (row == null) {
            return null;
        }
        return context.getApplication().getELResolver().getValue(context.getELContext(), row, field);
    }

    private void writePagination(ResponseWriter writer, int page, int pageSize, long total) throws IOException {
        long totalPages = total <= 0 ? 1 : (total + pageSize - 1) / pageSize;
        long firstRecord = total == 0 ? 0 : (long) page * pageSize + 1;
        long lastRecord = Math.min(total, (long) (page + 1) * pageSize);

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-datatable-pagination", null);
        writer.writeAttribute("data-current-page", String.valueOf(page), null);
        writer.writeAttribute("data-total-pages", String.valueOf(totalPages), null);
        writer.writeAttribute("data-first-record", String.valueOf(firstRecord), null);
        writer.writeAttribute("data-last-record", String.valueOf(lastRecord), null);
        writer.writeAttribute("data-total-records", String.valueOf(total), null);

        writePaginationButton(writer, "first", "«", page <= 0);
        writePaginationButton(writer, "previous", "‹", page <= 0);

        // Text filled in by datatable.js (initDatatable) from the data-*
        // attributes above - translated, so kept out of the server-rendered
        // markup on purpose (see Box.t in box-core.js).
        writer.startElement("span", this);
        writer.writeAttribute("class", "box-datatable-pagination-info", null);
        writer.endElement("span");

        writePaginationButton(writer, "next", "›", page + 1 >= totalPages);
        writePaginationButton(writer, "last", "»", page + 1 >= totalPages);

        writer.endElement("div");
    }

    private void writePaginationButton(ResponseWriter writer, String action, String label, boolean disabled)
            throws IOException {
        writer.startElement("button", this);
        writer.writeAttribute("type", "button", null);
        writer.writeAttribute("class", "box-datatable-pagination-button", null);
        writer.writeAttribute("data-action", action, null);
        if (disabled) {
            writer.writeAttribute("disabled", "disabled", null);
        }
        writer.writeText(label, null);
        writer.endElement("button");
    }

    private String renderTargetFor(String eventName, String clientId) {
        List<ClientBehavior> eventBehaviors = behaviors.get(eventName);
        if (eventBehaviors != null) {
            for (ClientBehavior behavior : eventBehaviors) {
                if (behavior instanceof AjaxBehavior ajax && ajax.getRender() != null
                        && !ajax.getRender().isEmpty()) {
                    return String.join(" ", ajax.getRender());
                }
            }
        }
        return "select".equals(eventName) ? "@none" : clientId;
    }
}
