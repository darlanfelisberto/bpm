package br.edu.iffar.box.component.button;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced button component supporting standard UICommand actions, f:ajax client
 * behaviors, and integrated icon rendering with customizable positioning.
 * Renders a native &lt;button&gt; tag so children like &lt;box-confirm&gt; can be nested.
 */
@FacesComponent(
        value = CommandButton.COMPONENT_TYPE,
        createTag = true,
        tagName = "commandButton",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "box.css", target = "head")
})
public class CommandButton extends UICommand implements ClientBehaviorHolder {

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
        writer.writeAttribute("value", clientId, null);

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
        List<ClientBehavior> actionBehaviors = getClientBehaviors().get("action");
        if (actionBehaviors == null || actionBehaviors.isEmpty()) {
            actionBehaviors = getClientBehaviors().get("click");
        }
        String behaviorScript = null;
        if (actionBehaviors != null && !actionBehaviors.isEmpty()) {
            ClientBehaviorContext behaviorContext = ClientBehaviorContext.createClientBehaviorContext(
                    context, this, "action", clientId, null);
            behaviorScript = actionBehaviors.get(0).getScript(behaviorContext);
        }

        boolean hasUserClick = userOnClick != null && !userOnClick.isBlank();
        boolean hasBehavior = behaviorScript != null && !behaviorScript.isBlank();

        if (!hasUserClick && !hasBehavior) {
            return null;
        }
        if (hasUserClick && !hasBehavior) {
            return userOnClick;
        }
        if (!hasUserClick && hasBehavior) {
            return behaviorScript;
        }
        return "var b=(function(){" + userOnClick + "})();if(b!==false){" + behaviorScript + "}return false;";
    }
}
