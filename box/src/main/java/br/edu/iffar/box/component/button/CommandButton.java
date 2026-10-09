package br.edu.iffar.box.component.button;

import br.edu.iffar.box.component.datatable.Datatable;
import br.edu.iffar.box.converter.EntityConverter;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.FacesEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced button component supporting standard UICommand actions, AJAX submissions
 * by default (like PrimeFaces p:commandButton), nested child tags like &lt;box-confirm&gt;,
 * and integrated icon rendering with customizable positioning.
 */
@FacesComponent(
        value = CommandButton.COMPONENT_TYPE,
        createTag = true,
        tagName = "commandButton",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "jakarta.faces", name = "faces.js", target = "head"),
        @ResourceDependency(library = "box", name = "box.css", target = "head")
})
public class CommandButton extends UICommand implements ClientBehaviorHolder {

    private static final Logger LOGGER = Logger.getLogger(CommandButton.class.getName());

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.CommandButton";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.CommandButton";

    private static final List<String> EVENT_NAMES = Collections.unmodifiableList(List.of(
            "action", "click", "dblclick", "focus", "blur"
    ));

    private final Map<String, List<ClientBehavior>> behaviors = new HashMap<>();

    public CommandButton() {
        setRendererType(null);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
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
        return "action";
    }

    public boolean isAjax() {
        Boolean ajax = (Boolean) getStateHelper().eval("ajax");
        return ajax == null || ajax;
    }

    public void setAjax(boolean ajax) {
        getStateHelper().put("ajax", ajax);
    }

    public String getRender() {
        String render = (String) getStateHelper().eval("render");
        if (render != null && !render.isBlank()) {
            return render;
        }
        return (String) getStateHelper().eval("update");
    }

    public void setRender(String render) {
        getStateHelper().put("render", render);
    }

    public String getUpdate() {
        return getRender();
    }

    public void setUpdate(String update) {
        setRender(update);
    }

    public String getExecute() {
        String execute = (String) getStateHelper().eval("execute");
        if (execute != null && !execute.isBlank()) {
            return execute;
        }
        return (String) getStateHelper().eval("process");
    }

    public void setExecute(String execute) {
        getStateHelper().put("execute", execute);
    }

    public String getProcess() {
        return getExecute();
    }

    public void setProcess(String process) {
        setExecute(process);
    }

    public boolean isResetValues() {
        Boolean reset = (Boolean) getStateHelper().eval("resetValues");
        return reset != null && reset;
    }

    public void setResetValues(boolean resetValues) {
        getStateHelper().put("resetValues", resetValues);
    }

    public String getOnevent() {
        return (String) getStateHelper().eval("onevent");
    }

    public void setOnevent(String onevent) {
        getStateHelper().put("onevent", onevent);
    }

    public String getOnerror() {
        return (String) getStateHelper().eval("onerror");
    }

    public void setOnerror(String onerror) {
        getStateHelper().put("onerror", onerror);
    }

    public String getLabel() {
        String label = (String) getStateHelper().eval("label");
        if (label != null) {
            return label;
        }
        Object value = getValue();
        return value != null ? value.toString() : null;
    }

    public void setLabel(String label) {
        getStateHelper().put("label", label);
    }

    public String getType() {
        String type = (String) getStateHelper().eval("type");
        return type != null && !type.isBlank() ? type : "submit";
    }

    public void setType(String type) {
        getStateHelper().put("type", type);
    }

    public boolean isDisabled() {
        Boolean disabled = (Boolean) getStateHelper().eval("disabled");
        return disabled != null && disabled;
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put("disabled", disabled);
    }

    public String getIcon() {
        return (String) getStateHelper().eval("icon");
    }

    public void setIcon(String icon) {
        getStateHelper().put("icon", icon);
    }

    public String getIconPos() {
        String pos = (String) getStateHelper().eval("iconPos");
        return pos != null && !pos.isBlank() ? pos : "left";
    }

    public void setIconPos(String iconPos) {
        getStateHelper().put("iconPos", iconPos);
    }

    public String getVariant() {
        return (String) getStateHelper().eval("variant");
    }

    public void setVariant(String variant) {
        getStateHelper().put("variant", variant);
    }

    public String getSize() {
        return (String) getStateHelper().eval("size");
    }

    public void setSize(String size) {
        getStateHelper().put("size", size);
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

    public String getTitle() {
        return (String) getStateHelper().eval("title");
    }

    public void setTitle(String title) {
        getStateHelper().put("title", title);
    }

    public String getAriaLabel() {
        return (String) getStateHelper().eval("ariaLabel");
    }

    public void setAriaLabel(String ariaLabel) {
        getStateHelper().put("ariaLabel", ariaLabel);
    }

    public String getTabindex() {
        Object val = getStateHelper().eval("tabindex");
        return val != null ? val.toString() : null;
    }

    public void setTabindex(String tabindex) {
        getStateHelper().put("tabindex", tabindex);
    }

    public String getOnclick() {
        return (String) getStateHelper().eval("onclick");
    }

    public void setOnclick(String onclick) {
        getStateHelper().put("onclick", onclick);
    }

    public String getOndblclick() {
        return (String) getStateHelper().eval("ondblclick");
    }

    public void setOndblclick(String ondblclick) {
        getStateHelper().put("ondblclick", ondblclick);
    }

    public String getOnfocus() {
        return (String) getStateHelper().eval("onfocus");
    }

    public void setOnfocus(String onfocus) {
        getStateHelper().put("onfocus", onfocus);
    }

    public String getOnblur() {
        return (String) getStateHelper().eval("onblur");
    }

    public void setOnblur(String onblur) {
        getStateHelper().put("onblur", onblur);
    }

    private transient String submittedRowId;

    public Object getRowValue() {
        return getStateHelper().eval("rowValue");
    }

    public void setRowValue(Object rowValue) {
        getStateHelper().put("rowValue", rowValue);
    }

    public String getVar() {
        return (String) getStateHelper().eval("var");
    }

    public void setVar(String var) {
        getStateHelper().put("var", var);
    }

    public Object getConverter() {
        return getStateHelper().eval("converter");
    }

    public void setConverter(Object converter) {
        getStateHelper().put("converter", converter);
    }

    public Object getTarget() {
        return getStateHelper().eval("target");
    }

    public void setTarget(Object target) {
        getStateHelper().put("target", target);
    }

    private Datatable findParentDatatable() {
        UIComponent current = getParent();
        while (current != null) {
            if (current instanceof Datatable datatable) {
                return datatable;
            }
            current = current.getParent();
        }
        return null;
    }

    public String resolveVar() {
        String var = getVar();
        if (var != null && !var.isBlank()) {
            return var;
        }
        Datatable dt = findParentDatatable();
        if (dt != null) {
            return dt.getVar();
        }
        return null;
    }

    public Object resolveRowValue(FacesContext context) {
        Object val = getRowValue();
        if (val != null) {
            return val;
        }
        String var = resolveVar();
        if (var != null) {
            return context.getExternalContext().getRequestMap().get(var);
        }
        return null;
    }

    public Converter resolveConverter(FacesContext context, Object value) {
        Object conv = getConverter();
        if (conv instanceof Converter c) {
            return c;
        }
        if (conv instanceof String convId && !convId.isBlank()) {
            Converter c = findConverterById(context, convId);
            if (c != null) {
                return c;
            }
        }
        Datatable dt = findParentDatatable();
        if (dt != null && dt.getConverter() != null) {
            return dt.getConverter();
        }
        if (value != null) {
            Converter c = context.getApplication().createConverter(value.getClass());
            if (c != null) {
                return c;
            }
        }
        try {
            Instance<EntityConverter> cdiInstance = CDI.current().select(EntityConverter.class);
            if (cdiInstance.isResolvable()) {
                return cdiInstance.get();
            }
            if (!cdiInstance.isUnsatisfied()) {
                return cdiInstance.iterator().next();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "CDI resolution of EntityConverter failed: " + e.getMessage(), e);
        }
        for (String elExpr : new String[]{"#{modelConverter}", "#{entityConverter}"}) {
            try {
                Object bean = context.getApplication().evaluateExpressionGet(context, elExpr, Object.class);
                if (bean instanceof Converter c) {
                    return c;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "EL evaluation of converter expression (" + elExpr + ") failed: " + e.getMessage(), e);
            }
        }
        for (String convId : new String[]{"modelConverter", "box.entityConverter"}) {
            Converter c = findConverterById(context, convId);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    private Converter findConverterById(FacesContext context, String converterId) {
        try {
            Converter c = context.getApplication().createConverter(converterId);
            if (c != null) {
                return c;
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Faces createConverter failed for ID '" + converterId + "': " + e.getMessage(), e);
        }
        try {
            Object bean = context.getApplication().evaluateExpressionGet(context, "#{" + converterId + "}", Object.class);
            if (bean instanceof Converter c) {
                return c;
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "EL evaluation of converter bean '#{" + converterId + "}' failed: " + e.getMessage(), e);
        }
        return null;
    }

    private String getConvertedRowValue(FacesContext context) {
        Object val = resolveRowValue(context);
        if (val == null) {
            return null;
        }
        Converter converter = resolveConverter(context, val);
        if (converter == null) {
            return val.toString();
        }
        return converter.getAsString(context, this, val);
    }

    private static String escapeJs(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("'", "\\'");
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered() || isDisabled()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        boolean sourceMatch = clientId.equals(params.get("jakarta.faces.source"));
        boolean nameMatch = params.containsKey(clientId);

        if (nameMatch || sourceMatch) {
            String rowId = params.get(clientId + "_row");
            if (rowId == null || rowId.isBlank()) {
                rowId = params.get("box_row");
            }
            if (rowId == null || rowId.isBlank()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (entry.getKey().endsWith(clientId + "_row") || entry.getKey().endsWith("box_row")) {
                        rowId = entry.getValue();
                        break;
                    }
                }
            }
            if (rowId == null || rowId.isBlank()) {
                rowId = params.get(clientId);
            }
            if (rowId != null && !rowId.isBlank() && !rowId.equals(clientId)) {
                this.submittedRowId = rowId;
            } else {
                this.submittedRowId = null;
            }
            queueEvent(new ActionEvent(this));
        }

        Map<String, List<ClientBehavior>> clientBehaviors = getClientBehaviors();
        if (!clientBehaviors.isEmpty()) {
            String eventName = params.get("jakarta.faces.behavior.event");
            if (eventName != null) {
                List<ClientBehavior> behaviorsForEvent = clientBehaviors.get(eventName);
                if (behaviorsForEvent != null) {
                    for (ClientBehavior behavior : behaviorsForEvent) {
                        behavior.decode(context, this);
                    }
                }
            }
        }
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (event instanceof ActionEvent) {
            FacesContext context = getFacesContext();
            String var = resolveVar();
            Object rowObject = null;

            if (submittedRowId != null) {
                Converter converter = resolveConverter(context, null);
                if (converter != null) {
                    rowObject = converter.getAsObject(context, this, submittedRowId);
                }
            }

            ValueExpression target = getValueExpression("target");
            if (target != null && rowObject != null) {
                target.setValue(context.getELContext(), rowObject);
            }

            Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
            boolean restoreVar = false;
            Object previousVarValue = null;

            if (var != null && rowObject != null) {
                restoreVar = requestMap.containsKey(var);
                previousVarValue = requestMap.get(var);
                requestMap.put(var, rowObject);
            }

            try {
                super.broadcast(event);
            } catch (MethodNotFoundException e) {
                MethodExpression actionExpression = getActionExpression();
                if (actionExpression != null && rowObject != null) {
                    actionExpression.invoke(context.getELContext(), new Object[]{rowObject});
                } else {
                    throw e;
                }
            } finally {
                if (var != null && rowObject != null) {
                    if (restoreVar) {
                        requestMap.put(var, previousVarValue);
                    } else {
                        requestMap.remove(var);
                    }
                }
            }
        } else {
            super.broadcast(event);
        }
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        String clientId = getClientId(context);
        boolean disabled = isDisabled();
        String type = getType();

        writer.startElement("button", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("name", clientId, "name");
        writer.writeAttribute("type", type, "type");

        String convertedRow = getConvertedRowValue(context);
        if (convertedRow != null) {
            writer.writeAttribute("value", convertedRow, null);
            writer.writeAttribute("data-row-value", convertedRow, null);
        } else {
            writer.writeAttribute("value", clientId, null);
        }

        if (disabled) {
            writer.writeAttribute("disabled", "disabled", null);
            writer.writeAttribute("aria-disabled", "true", null);
        }

        String label = getLabel();
        boolean hasLabel = label != null && !label.isBlank();
        String icon = getIcon();
        boolean hasIcon = icon != null && !icon.isBlank();

        StringBuilder classes = new StringBuilder("box-button");
        String variant = getVariant();
        if (variant != null && !variant.isBlank()) {
            classes.append(" box-button-").append(variant.trim().toLowerCase());
        }
        String size = getSize();
        if (size != null && !size.isBlank()) {
            classes.append(" box-button-").append(size.trim().toLowerCase());
        }
        if (hasIcon && !hasLabel) {
            classes.append(" box-button-icon-only");
        }
        String styleClass = getStyleClass();
        if (styleClass != null && !styleClass.isBlank()) {
            classes.append(" ").append(styleClass.trim());
        }
        writer.writeAttribute("class", classes.toString(), null);

        String style = getStyle();
        if (style != null && !style.isBlank()) {
            writer.writeAttribute("style", style, null);
        }

        String title = getTitle();
        if (title != null && !title.isBlank()) {
            writer.writeAttribute("title", title, null);
        }

        String ariaLabel = getAriaLabel();
        if (ariaLabel != null && !ariaLabel.isBlank()) {
            writer.writeAttribute("aria-label", ariaLabel, null);
        } else if (!hasLabel && title != null && !title.isBlank()) {
            writer.writeAttribute("aria-label", title, null);
        }

        String tabindex = getTabindex();
        if (tabindex != null && !tabindex.isBlank()) {
            writer.writeAttribute("tabindex", tabindex, null);
        }

        String onClickScript = buildOnClickScript(context, clientId, getOnclick());
        if (onClickScript != null && !onClickScript.isBlank()) {
            writer.writeAttribute("onclick", onClickScript, null);
        }

        writeAttributeIfPresent(writer, "ondblclick", getOndblclick());
        writeAttributeIfPresent(writer, "onfocus", getOnfocus());
        writeAttributeIfPresent(writer, "onblur", getOnblur());

        boolean isIconRight = "right".equalsIgnoreCase(getIconPos());

        if (hasIcon && !isIconRight) {
            renderIcon(writer, icon, hasLabel ? "box-button-icon-left" : "box-button-icon");
        }

        if (hasLabel) {
            writer.startElement("span", this);
            writer.writeAttribute("class", "box-button-label", null);
            writer.writeText(label, null);
            writer.endElement("span");
        }

        if (hasIcon && isIconRight) {
            renderIcon(writer, icon, hasLabel ? "box-button-icon-right" : "box-button-icon");
        }
    }

    private void renderIcon(ResponseWriter writer, String icon, String extraClass) throws IOException {
        writer.startElement("i", this);
        writer.writeAttribute("class", icon + (extraClass != null ? " " + extraClass : ""), null);
        writer.writeAttribute("aria-hidden", "true", null);
        writer.endElement("i");
    }

    private void writeAttributeIfPresent(ResponseWriter writer, String name, String value) throws IOException {
        if (value != null && !value.isBlank()) {
            writer.writeAttribute(name, value, null);
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("button");
    }

    private String buildOnClickScript(FacesContext context, String clientId, String userOnClick) {
        if ("reset".equalsIgnoreCase(getType())) {
            return userOnClick;
        }

        boolean ajax = isAjax();
        String ajaxScript = null;

        if (ajax) {
            List<ClientBehavior> actionBehaviors = getClientBehaviors().get("action");
            if (actionBehaviors == null || actionBehaviors.isEmpty()) {
                actionBehaviors = getClientBehaviors().get("click");
            }

            if (actionBehaviors != null && !actionBehaviors.isEmpty()) {
                ClientBehaviorContext behaviorContext = ClientBehaviorContext.createClientBehaviorContext(
                        context, this, "action", clientId, null);
                ajaxScript = actionBehaviors.get(0).getScript(behaviorContext);
            } else {
                ajaxScript = buildDefaultAjaxScript(context);
            }
        }

        boolean hasUserClick = userOnClick != null && !userOnClick.isBlank();
        boolean hasAjax = ajaxScript != null && !ajaxScript.isBlank();

        if (!hasUserClick && !hasAjax) {
            return null;
        }
        if (hasUserClick && !hasAjax) {
            return userOnClick;
        }
        if (!hasUserClick && hasAjax) {
            return ajaxScript;
        }
        return "var b=(function(){" + userOnClick + "})();if(b!==false){" + ajaxScript + "}return false;";
    }

    private String buildDefaultAjaxScript(FacesContext context) {
        String execute = resolveClientIds(context, getExecute(), "@form");
        String render = resolveClientIds(context, getRender(), "@form");
        String clientId = getClientId(context);

        StringBuilder sb = new StringBuilder("faces.ajax.request(this,event,{");
        sb.append("'jakarta.faces.behavior.event':'action'");
        sb.append(",execute:'").append(execute).append("'");
        sb.append(",render:'").append(render).append("'");

        String convertedRow = getConvertedRowValue(context);
        if (convertedRow != null) {
            String escaped = escapeJs(convertedRow);
            sb.append(",params:{'").append(clientId).append("_row':'").append(escaped).append("','box_row':'").append(escaped).append("'}");
            sb.append(",'").append(clientId).append("_row':'").append(escaped).append("'");
            sb.append(",box_row:'").append(escaped).append("'");
        }

        if (isResetValues()) {
            sb.append(",resetValues:true");
        }
        String onevent = getOnevent();
        if (onevent != null && !onevent.isBlank()) {
            sb.append(",onevent:").append(onevent.trim());
        }
        String onerror = getOnerror();
        if (onerror != null && !onerror.isBlank()) {
            sb.append(",onerror:").append(onerror.trim());
        }
        sb.append("});return false;");
        return sb.toString();
    }

    private String resolveClientIds(FacesContext context, String expressions, String defaultKeyword) {
        if (expressions == null || expressions.isBlank()) {
            return defaultKeyword;
        }
        String[] tokens = expressions.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            if (token.startsWith("@")) {
                sb.append(token);
            } else {
                UIComponent target = findComponent(token);
                if (target != null) {
                    sb.append(target.getClientId(context));
                } else if (token.startsWith(":")) {
                    sb.append(token.substring(1));
                } else {
                    sb.append(token);
                }
            }
        }
        return sb.toString();
    }
}
