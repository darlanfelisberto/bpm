package br.edu.iffar.box.component.autocomplete;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Autocomplete input with dynamic suggestions, equivalent to PrimeFaces'
 * p:autoComplete. Integrates with JSF form submission (UIInput) and supports
 * string lists or POJOs with a Converter.
 *
 * Suggestions are fetched dynamically from the server via completeMethod on
 * every debounced keystroke or dropdown button click.
 *
 * Client behaviors: "itemSelect" (fired when a suggestion is selected — default
 * event), "change" (input value changed), "query" (search query request) and
 * "clear" (value cleared).
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <b:autocomplete value="#{bean.selectedPerson}"
 *                      completeMethod="#{bean.completePerson}"
 *                      var="p" itemLabel="#{p.name}" itemValue="#{p}"
 *                      converter="#{personConverter}"
 *                      dropdown="true" placeholder="Search person...">
 *          <f:ajax event="itemSelect" listener="#{bean.onSelect}" render=":result"/>
 *      </b:autocomplete>
 */
@FacesComponent(
        value = Autocomplete.COMPONENT_TYPE,
        createTag = true,
        tagName = "autocomplete",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "autocomplete/autocomplete.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "autocomplete/autocomplete.js", target = "head")
})
public class Autocomplete extends UIInput implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Autocomplete";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Autocomplete";

    private static final List<String> EVENT_NAMES =
            Collections.unmodifiableList(List.of("itemSelect", "change", "query", "clear"));

    private final Map<String, List<ClientBehavior>> behaviors = new HashMap<>();

    private transient List<?> queryResults;
    private transient String currentQuery;

    public Autocomplete() {
        setRendererType(null);
    }

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
        return "itemSelect";
    }

    public MethodExpression getCompleteMethod() {
        return (MethodExpression) getStateHelper().get("completeMethod");
    }

    public void setCompleteMethod(MethodExpression completeMethod) {
        getStateHelper().put("completeMethod", completeMethod);
    }

    public String getVar() {
        return (String) getStateHelper().eval("var");
    }

    public void setVar(String var) {
        getStateHelper().put("var", var);
    }

    public String getItemLabel() {
        return (String) getStateHelper().eval("itemLabel");
    }

    public void setItemLabel(String itemLabel) {
        getStateHelper().put("itemLabel", itemLabel);
    }

    public Object getItemValue() {
        return getStateHelper().eval("itemValue");
    }

    public void setItemValue(Object itemValue) {
        getStateHelper().put("itemValue", itemValue);
    }

    public int getMinQueryLength() {
        Integer value = (Integer) getStateHelper().eval("minQueryLength");
        return value != null ? value : 1;
    }

    public void setMinQueryLength(int minQueryLength) {
        getStateHelper().put("minQueryLength", minQueryLength);
    }

    public int getQueryDelay() {
        Integer value = (Integer) getStateHelper().eval("queryDelay");
        return value != null ? value : 300;
    }

    public void setQueryDelay(int queryDelay) {
        getStateHelper().put("queryDelay", queryDelay);
    }

    public boolean isDropdown() {
        Boolean value = (Boolean) getStateHelper().eval("dropdown");
        return value != null && value;
    }

    public void setDropdown(boolean dropdown) {
        getStateHelper().put("dropdown", dropdown);
    }

    public String getDropdownMode() {
        String value = (String) getStateHelper().eval("dropdownMode");
        return value != null ? value : "blank";
    }

    public void setDropdownMode(String dropdownMode) {
        getStateHelper().put("dropdownMode", dropdownMode);
    }

    public boolean isForceSelection() {
        Boolean value = (Boolean) getStateHelper().eval("forceSelection");
        return value != null && value;
    }

    public void setForceSelection(boolean forceSelection) {
        getStateHelper().put("forceSelection", forceSelection);
    }

    public String getPlaceholder() {
        return (String) getStateHelper().eval("placeholder");
    }

    public void setPlaceholder(String placeholder) {
        getStateHelper().put("placeholder", placeholder);
    }

    public String getEmptyMessage() {
        return (String) getStateHelper().eval("emptyMessage");
    }

    public void setEmptyMessage(String emptyMessage) {
        getStateHelper().put("emptyMessage", emptyMessage);
    }

    public boolean isDisabled() {
        Boolean value = (Boolean) getStateHelper().eval("disabled");
        return value != null && value;
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put("disabled", disabled);
    }

    public boolean isReadonly() {
        Boolean value = (Boolean) getStateHelper().eval("readonly");
        return value != null && value;
    }

    public void setReadonly(boolean readonly) {
        getStateHelper().put("readonly", readonly);
    }

    public Integer getMaxResults() {
        return (Integer) getStateHelper().eval("maxResults");
    }

    public void setMaxResults(Integer maxResults) {
        getStateHelper().put("maxResults", maxResults);
    }

    public String getStyle() {
        return (String) getStateHelper().eval("style");
    }

    public void setStyle(String style) {
        getStateHelper().put("style", style);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval("styleClass");
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put("styleClass", styleClass);
    }

    public String getInputStyle() {
        return (String) getStateHelper().eval("inputStyle");
    }

    public void setInputStyle(String inputStyle) {
        getStateHelper().put("inputStyle", inputStyle);
    }

    public String getInputStyleClass() {
        return (String) getStateHelper().eval("inputStyleClass");
    }

    public void setInputStyleClass(String inputStyleClass) {
        getStateHelper().put("inputStyleClass", inputStyleClass);
    }

    public Converter resolveConverter(FacesContext context) {
        Converter converter = super.getConverter();
        if (converter != null) {
            return converter;
        }
        Object val = getStateHelper().eval("converter");
        if (val instanceof Converter c) {
            return c;
        } else if (val instanceof String converterId && !converterId.isBlank()) {
            Converter c = context.getApplication().createConverter(converterId);
            if (c != null) {
                return c;
            }
        }
        ValueExpression ve = getValueExpression("converter");
        if (ve != null) {
            ELContext elContext = context.getELContext();
            Object evalResult = ve.getValue(elContext);
            if (evalResult instanceof Converter c) {
                return c;
            } else if (evalResult instanceof String converterId && !converterId.isBlank()) {
                Converter c = context.getApplication().createConverter(converterId);
                if (c != null) {
                    return c;
                }
            }
        }
        ValueExpression valueExpr = getValueExpression("value");
        if (valueExpr != null) {
            Class<?> type = valueExpr.getType(context.getELContext());
            if (type != null && type != Object.class && type != String.class) {
                Converter c = context.getApplication().createConverter(type);
                if (c != null) {
                    return c;
                }
            }
        }
        return null;
    }

    @Override
    public Converter getConverter() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            return resolveConverter(context);
        }
        return super.getConverter();
    }

    @Override
    public void setConverter(Converter converter) {
        getStateHelper().put("converter", converter);
        super.setConverter(converter);
    }

    @Override
    public Object getConvertedValue(FacesContext context, Object newSubmittedValue) throws ConverterException {
        String submittedString = (String) newSubmittedValue;
        if (submittedString == null || submittedString.isBlank()) {
            return null;
        }
        Converter converter = resolveConverter(context);
        if (converter != null) {
            return converter.getAsObject(context, this, submittedString);
        }
        return super.getConvertedValue(context, newSubmittedValue);
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered() || isDisabled()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();

        if (clientId.equals(parameters.get("jakarta.faces.source"))) {
            String eventName = parameters.get("jakarta.faces.behavior.event");
            if (eventName != null) {
                switch (eventName) {
                    case "query" -> {
                        currentQuery = parameters.get(clientId + "_query");
                        queryResults = executeCompleteMethod(context, currentQuery != null ? currentQuery : "");
                    }
                    case "itemSelect" -> {
                        String selected = parameters.get(clientId + "_itemSelect");
                        if (selected == null) {
                            selected = parameters.get(clientId);
                        }
                        setSubmittedValue(selected);
                    }
                    case "clear" -> setSubmittedValue("");
                    case "change" -> {
                        String val = parameters.get(clientId);
                        if (val == null) {
                            val = parameters.get(clientId + "_value");
                        }
                        setSubmittedValue(val != null ? val : "");
                    }
                    default -> { }
                }

                List<ClientBehavior> behaviorsForEvent = behaviors.get(eventName);
                if (behaviorsForEvent != null) {
                    for (ClientBehavior behavior : behaviorsForEvent) {
                        behavior.decode(context, this);
                    }
                }
                return;
            }
        }

        if (parameters.containsKey(clientId)) {
            setSubmittedValue(parameters.get(clientId));
        }
    }

    @SuppressWarnings("unchecked")
    List<?> executeCompleteMethod(FacesContext context, String query) {
        MethodExpression methodExpression = getCompleteMethod();
        if (methodExpression == null) {
            Object stored = getStateHelper().get("completeMethod");
            if (stored instanceof MethodExpression me) {
                methodExpression = me;
            } else {
                ValueExpression ve = getValueExpression("completeMethod");
                if (ve != null) {
                    ELContext elContext = context.getELContext();
                    methodExpression = context.getApplication().getExpressionFactory()
                            .createMethodExpression(elContext, ve.getExpressionString(), List.class, new Class<?>[]{ String.class });
                }
            }
        }
        if (methodExpression == null) {
            return Collections.emptyList();
        }
        ELContext elContext = context.getELContext();
        Object result = null;
        try {
            result = methodExpression.invoke(elContext, new Object[]{ query });
        } catch (MethodNotFoundException e) {
            try {
                result = methodExpression.invoke(elContext, new Object[]{});
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            try {
                result = methodExpression.invoke(elContext, new Object[]{});
            } catch (Exception ignored) {
            }
        }
        if (result instanceof List<?> list) {
            return limitResults(list, getMaxResults());
        }
        return Collections.emptyList();
    }

    static List<?> limitResults(List<?> list, Integer maxResults) {
        if (list == null) {
            return Collections.emptyList();
        }
        if (maxResults != null && maxResults > 0 && list.size() > maxResults) {
            return list.subList(0, maxResults);
        }
        return list;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        String clientId = getClientId(context);
        Object currentValue = getValue();
        String displayLabel = resolveDisplayLabel(context, currentValue);
        String serializedValue = resolveSerializedValue(context, currentValue);

        String styleClass = getStyleClass();
        String rootClass = "box-autocomplete" + (styleClass != null && !styleClass.isBlank() ? " " + styleClass : "");

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", rootClass, null);
        if (getStyle() != null) {
            writer.writeAttribute("style", getStyle(), null);
        }

        writer.writeAttribute("data-min-query-length", String.valueOf(getMinQueryLength()), null);
        writer.writeAttribute("data-query-delay", String.valueOf(getQueryDelay()), null);
        writer.writeAttribute("data-dropdown", String.valueOf(isDropdown()), null);
        writer.writeAttribute("data-dropdown-mode", getDropdownMode(), null);
        writer.writeAttribute("data-force-selection", String.valueOf(isForceSelection()), null);
        writer.writeAttribute("data-disabled", String.valueOf(isDisabled()), null);
        writer.writeAttribute("data-selected-label", displayLabel, null);
        writer.writeAttribute("data-selected-value", serializedValue, null);

        for (String eventName : EVENT_NAMES) {
            writer.writeAttribute("data-render-" + eventName, renderTargetFor(eventName, clientId), null);
        }

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-autocomplete-wrap", null);

        writer.startElement("input", this);
        writer.writeAttribute("type", "text", null);
        writer.writeAttribute("id", clientId + "_input", "id");
        String inputClass = "box-autocomplete-input" + (getInputStyleClass() != null && !getInputStyleClass().isBlank() ? " " + getInputStyleClass() : "");
        writer.writeAttribute("class", inputClass, null);
        if (getInputStyle() != null) {
            writer.writeAttribute("style", getInputStyle(), null);
        }
        writer.writeAttribute("autocomplete", "off", null);
        writer.writeAttribute("spellcheck", "false", null);
        writer.writeAttribute("value", displayLabel, null);
        if (getPlaceholder() != null) {
            writer.writeAttribute("placeholder", getPlaceholder(), null);
        }
        if (isDisabled()) {
            writer.writeAttribute("disabled", "disabled", null);
        }
        if (isReadonly()) {
            writer.writeAttribute("readonly", "readonly", null);
        }
        writer.endElement("input");

        writer.startElement("input", this);
        writer.writeAttribute("type", "hidden", null);
        writer.writeAttribute("id", clientId + "_value", "id");
        writer.writeAttribute("name", clientId, null);
        writer.writeAttribute("class", "box-autocomplete-value", null);
        writer.writeAttribute("value", serializedValue, null);
        writer.endElement("input");

        if (!isDisabled() && !isReadonly()) {
            writer.startElement("button", this);
            writer.writeAttribute("type", "button", null);
            writer.writeAttribute("class", "box-autocomplete-clear", null);
            writer.writeAttribute("tabindex", "-1", null);
            writer.writeAttribute("data-box-i18n", "autocomplete.clear", null);
            writer.writeAttribute("data-box-i18n-attr", "aria-label", null);
            if (displayLabel.isEmpty()) {
                writer.writeAttribute("style", "display:none;", null);
            }
            writer.writeText("×", null);
            writer.endElement("button");
        }

        writer.startElement("span", this);
        writer.writeAttribute("class", "box-autocomplete-loading", null);
        writer.writeAttribute("role", "status", null);
        writer.writeAttribute("data-box-i18n", "autocomplete.loading", null);
        writer.writeAttribute("data-box-i18n-attr", "aria-label", null);
        writer.writeAttribute("style", "display:none;", null);
        writer.endElement("span");

        if (isDropdown() && !isReadonly()) {
            writer.startElement("button", this);
            writer.writeAttribute("type", "button", null);
            writer.writeAttribute("class", "box-autocomplete-dropdown", null);
            writer.writeAttribute("tabindex", "-1", null);
            writer.writeAttribute("aria-haspopup", "listbox", null);
            writer.writeAttribute("aria-expanded", "false", null);
            if (isDisabled()) {
                writer.writeAttribute("disabled", "disabled", null);
            }
            writer.writeText("▾", null);
            writer.endElement("button");
        }

        writer.endElement("div"); // .box-autocomplete-wrap

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId + "_panel", "id");
        writer.writeAttribute("class", "box-autocomplete-panel", null);
        if (queryResults == null) {
            writer.writeAttribute("style", "display:none;", null);
        } else {
            writeSuggestions(context, writer, queryResults);
        }
        writer.endElement("div"); // .box-autocomplete-panel
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        // Rendered explicitly within writeSuggestions per item
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div");
    }

    private void writeSuggestions(FacesContext context, ResponseWriter writer, List<?> suggestions) throws IOException {
        if (suggestions == null) {
            return;
        }
        if (suggestions.isEmpty()) {
            String emptyMsg = getEmptyMessage();
            writer.startElement("div", this);
            writer.writeAttribute("class", "box-autocomplete-empty", null);
            if (emptyMsg != null && !emptyMsg.isBlank()) {
                writer.writeText(emptyMsg, null);
            } else {
                writer.writeAttribute("data-box-i18n", "autocomplete.noResults", null);
                writer.writeText("No results found", null);
            }
            writer.endElement("div");
            return;
        }

        String var = getVar();
        ValueExpression itemLabelExpr = getValueExpression("itemLabel");
        ValueExpression itemValueExpr = getValueExpression("itemValue");
        Converter converter = resolveConverter(context);
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        boolean hasChildren = getChildCount() > 0;

        writer.startElement("ul", this);
        writer.writeAttribute("class", "box-autocomplete-list", null);
        writer.writeAttribute("role", "listbox", null);

        for (Object item : suggestions) {
            boolean hadPrev = var != null && requestMap.containsKey(var);
            Object prev = hadPrev ? requestMap.get(var) : null;
            if (var != null) {
                requestMap.put(var, item);
            }
            try {
                String label;
                if (itemLabelExpr != null) {
                    Object lbl = itemLabelExpr.getValue(context.getELContext());
                    label = lbl != null ? lbl.toString() : "";
                } else if (converter != null) {
                    label = converter.getAsString(context, this, item);
                } else {
                    label = item != null ? item.toString() : "";
                }

                Object val = itemValueExpr != null ? itemValueExpr.getValue(context.getELContext()) : item;
                String key;
                if (converter != null) {
                    key = converter.getAsString(context, this, val);
                } else {
                    key = val != null ? val.toString() : "";
                }

                writer.startElement("li", this);
                writer.writeAttribute("class", "box-autocomplete-item", null);
                writer.writeAttribute("role", "option", null);
                writer.writeAttribute("data-item-value", key, null);
                writer.writeAttribute("data-item-label", label, null);

                if (hasChildren) {
                    for (UIComponent child : getChildren()) {
                        if (child.isRendered()) {
                            child.encodeAll(context);
                        }
                    }
                } else {
                    writer.writeText(label, null);
                }

                writer.endElement("li");
            } finally {
                if (var != null) {
                    if (hadPrev) {
                        requestMap.put(var, prev);
                    } else {
                        requestMap.remove(var);
                    }
                }
            }
        }

        writer.endElement("ul");
    }

    private String resolveDisplayLabel(FacesContext context, Object currentValue) {
        if (currentValue == null) {
            return "";
        }
        String var = getVar();
        ValueExpression itemLabelExpr = getValueExpression("itemLabel");
        if (var != null && itemLabelExpr != null) {
            Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
            boolean hadPrev = requestMap.containsKey(var);
            Object prev = hadPrev ? requestMap.get(var) : null;
            try {
                requestMap.put(var, currentValue);
                Object label = itemLabelExpr.getValue(context.getELContext());
                return label != null ? label.toString() : "";
            } finally {
                if (hadPrev) {
                    requestMap.put(var, prev);
                } else {
                    requestMap.remove(var);
                }
            }
        }
        Converter converter = resolveConverter(context);
        if (converter != null) {
            return converter.getAsString(context, this, currentValue);
        }
        return currentValue.toString();
    }

    private String resolveSerializedValue(FacesContext context, Object currentValue) {
        if (currentValue == null) {
            return "";
        }
        Converter converter = resolveConverter(context);
        if (converter != null) {
            return converter.getAsString(context, this, currentValue);
        }
        String var = getVar();
        ValueExpression itemValueExpr = getValueExpression("itemValue");
        if (var != null && itemValueExpr != null) {
            Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
            boolean hadPrev = requestMap.containsKey(var);
            Object prev = hadPrev ? requestMap.get(var) : null;
            try {
                requestMap.put(var, currentValue);
                Object val = itemValueExpr.getValue(context.getELContext());
                return val != null ? val.toString() : "";
            } finally {
                if (hadPrev) {
                    requestMap.put(var, prev);
                } else {
                    requestMap.remove(var);
                }
            }
        }
        return currentValue.toString();
    }

    private String renderTargetFor(String eventName, String clientId) {
        List<ClientBehavior> behaviorsForEvent = behaviors.get(eventName);
        if (behaviorsForEvent != null) {
            for (ClientBehavior behavior : behaviorsForEvent) {
                if (behavior instanceof AjaxBehavior ajax && ajax.getRender() != null
                        && !ajax.getRender().isEmpty()) {
                    return String.join(" ", ajax.getRender());
                }
            }
        }
        return "query".equals(eventName) ? clientId : "@none";
    }
}
