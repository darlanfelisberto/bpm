package br.edu.iffar.box.component.menu;

import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Individual item inside a b:menu or b:submenu. Supports bookmarkable navigation
 * (outcome, url) and server/ajax actions (action, actionListener, f:ajax).
 */
@FacesComponent(
        value = MenuItem.COMPONENT_TYPE,
        createTag = true,
        tagName = "menuitem",
        namespace = "http://iffar.edu.br/box")
public class MenuItem extends UICommand implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.MenuItem";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Menu";

    private static final List<String> EVENT_NAMES = Collections.unmodifiableList(List.of("action", "click"));
    private final Map<String, List<ClientBehavior>> behaviors = new HashMap<>();

    public MenuItem() {
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

    public String getOutcome() {
        return (String) getStateHelper().eval("outcome");
    }

    public void setOutcome(String outcome) {
        getStateHelper().put("outcome", outcome);
    }

    public String getUrl() {
        String url = (String) getStateHelper().eval("url");
        if (url != null) {
            return url;
        }
        return (String) getStateHelper().eval("href");
    }

    public void setUrl(String url) {
        getStateHelper().put("url", url);
    }

    public String getHref() {
        return getUrl();
    }

    public void setHref(String href) {
        setUrl(href);
    }

    public String getIcon() {
        return (String) getStateHelper().eval("icon");
    }

    public void setIcon(String icon) {
        getStateHelper().put("icon", icon);
    }

    public String getBadge() {
        return (String) getStateHelper().eval("badge");
    }

    public void setBadge(String badge) {
        getStateHelper().put("badge", badge);
    }

    public String getBadgeClass() {
        return (String) getStateHelper().eval("badgeClass");
    }

    public void setBadgeClass(String badgeClass) {
        getStateHelper().put("badgeClass", badgeClass);
    }

    public String getTarget() {
        return (String) getStateHelper().eval("target");
    }

    public void setTarget(String target) {
        getStateHelper().put("target", target);
    }

    public boolean isDisabled() {
        Boolean disabled = (Boolean) getStateHelper().eval("disabled");
        return disabled != null && disabled;
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put("disabled", disabled);
    }

    public Boolean getActive() {
        return (Boolean) getStateHelper().eval("active");
    }

    public void setActive(Boolean active) {
        getStateHelper().put("active", active);
    }

    public String getTitle() {
        return (String) getStateHelper().eval("title");
    }

    public void setTitle(String title) {
        getStateHelper().put("title", title);
    }

    public String getOnclick() {
        return (String) getStateHelper().eval("onclick");
    }

    public void setOnclick(String onclick) {
        getStateHelper().put("onclick", onclick);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval("styleClass");
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put("styleClass", styleClass);
    }

    public String getStyle() {
        return (String) getStateHelper().eval("style");
    }

    public void setStyle(String style) {
        getStateHelper().put("style", style);
    }

    public boolean isActive(FacesContext context) {
        Boolean explicitActive = getActive();
        if (explicitActive != null) {
            return explicitActive;
        }
        String outcome = getOutcome();
        if (outcome != null && !outcome.isBlank() && context != null && context.getViewRoot() != null) {
            String currentViewId = context.getViewRoot().getViewId();
            if (currentViewId != null) {
                String normOutcome = normalizeViewId(outcome);
                String normCurrent = normalizeViewId(currentViewId);
                return normCurrent.equals(normOutcome);
            }
        }
        return false;
    }

    static String normalizeViewId(String viewId) {
        if (viewId == null) {
            return "";
        }
        String normalized = viewId.startsWith("/") ? viewId : "/" + viewId;
        if (normalized.endsWith(".xhtml")) {
            normalized = normalized.substring(0, normalized.length() - 6);
        }
        return normalized;
    }

    public String resolveHref(FacesContext context) {
        if (context == null) {
            return null;
        }
        String outcome = getOutcome();
        if (outcome != null && !outcome.isBlank()) {
            ConfigurableNavigationHandler navHandler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
            NavigationCase navCase = navHandler.getNavigationCase(context, null, outcome);
            if (navCase != null) {
                try {
                    URL bookmarkableUrl = navCase.getBookmarkableURL(context);
                    if (bookmarkableUrl != null) {
                        return bookmarkableUrl.toExternalForm();
                    }
                } catch (MalformedURLException e) {
                    // Falls through to ViewHandler resolution
                }
            }
            return context.getApplication().getViewHandler().getBookmarkableURL(context, outcome, null, false);
        }
        String url = getUrl();
        if (url != null && !url.isBlank()) {
            if (url.startsWith("/") && !url.startsWith("//")) {
                String cp = context.getExternalContext().getRequestContextPath();
                if (cp != null && !cp.isEmpty() && !url.startsWith(cp + "/")) {
                    return cp + url;
                }
            }
            return url;
        }
        return null;
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered() || isDisabled()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        if (params.containsKey(clientId) || clientId.equals(params.get("jakarta.faces.source"))) {
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
        boolean active = isActive(context);
        String styleClass = getStyleClass();
        String style = getStyle();

        writer.startElement("li", this);
        StringBuilder liClass = new StringBuilder("box-menu-item");
        if (disabled) {
            liClass.append(" box-menu-disabled");
        }
        if (active) {
            liClass.append(" box-menu-active");
        }
        if (styleClass != null && !styleClass.isBlank()) {
            liClass.append(" ").append(styleClass.trim());
        }
        writer.writeAttribute("class", liClass.toString(), null);
        if (style != null && !style.isBlank()) {
            writer.writeAttribute("style", style, null);
        }

        String href = resolveHref(context);
        String title = getTitle();
        String userOnClick = getOnclick();

        if (href != null) {
            writer.startElement("a", this);
            writer.writeAttribute("id", clientId, "id");
            writer.writeAttribute("class", "box-menu-link" + (active ? " active" : ""), null);
            if (!disabled) {
                writer.writeAttribute("href", href, null);
                String target = getTarget();
                if (target != null && !target.isBlank()) {
                    writer.writeAttribute("target", target, null);
                    if ("_blank".equalsIgnoreCase(target)) {
                        writer.writeAttribute("rel", "noopener noreferrer", null);
                    }
                }
            } else {
                writer.writeAttribute("aria-disabled", "true", null);
                writer.writeAttribute("tabindex", "-1", null);
            }
            if (title != null && !title.isBlank()) {
                writer.writeAttribute("title", title, null);
            }
            if (userOnClick != null && !userOnClick.isBlank()) {
                writer.writeAttribute("onclick", userOnClick, null);
            }
        } else {
            writer.startElement("button", this);
            writer.writeAttribute("type", "submit", null);
            writer.writeAttribute("id", clientId, "id");
            writer.writeAttribute("name", clientId, null);
            writer.writeAttribute("class", "box-menu-link box-menu-button" + (active ? " active" : ""), null);
            if (disabled) {
                writer.writeAttribute("disabled", "disabled", null);
                writer.writeAttribute("aria-disabled", "true", null);
            }
            if (title != null && !title.isBlank()) {
                writer.writeAttribute("title", title, null);
            }

            String onClickScript = buildOnClickScript(context, clientId, userOnClick);
            if (onClickScript != null && !onClickScript.isBlank()) {
                writer.writeAttribute("onclick", onClickScript, null);
            }
        }

        String icon = getIcon();
        if (icon != null && !icon.isBlank()) {
            writer.startElement("i", this);
            writer.writeAttribute("class", icon + " box-menu-icon", null);
            writer.writeAttribute("aria-hidden", "true", null);
            writer.endElement("i");
        }

        String label = getLabel();
        if (label != null && !label.isBlank()) {
            writer.startElement("span", this);
            writer.writeAttribute("class", "box-menu-label", null);
            writer.writeText(label, null);
            writer.endElement("span");
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();

        String badge = getBadge();
        if (badge != null && !badge.isBlank()) {
            writer.startElement("span", this);
            String badgeClass = getBadgeClass();
            writer.writeAttribute("class", "box-menu-badge" + (badgeClass != null && !badgeClass.isBlank() ? " " + badgeClass.trim() : ""), null);
            writer.writeText(badge, null);
            writer.endElement("span");
        }

        String href = resolveHref(context);
        if (href != null) {
            writer.endElement("a");
        } else {
            writer.endElement("button");
        }

        writer.endElement("li");
    }

    private String buildOnClickScript(FacesContext context, String clientId, String userOnClick) {
        StringBuilder script = new StringBuilder();
        if (userOnClick != null && !userOnClick.isBlank()) {
            script.append(userOnClick);
            if (!userOnClick.trim().endsWith(";")) {
                script.append(";");
            }
        }
        List<ClientBehavior> actionBehaviors = getClientBehaviors().get("action");
        if (actionBehaviors != null && !actionBehaviors.isEmpty()) {
            ClientBehaviorContext behaviorContext = ClientBehaviorContext.createClientBehaviorContext(
                    context, this, "action", clientId, null);
            String behaviorScript = actionBehaviors.get(0).getScript(behaviorContext);
            if (behaviorScript != null && !behaviorScript.isBlank()) {
                script.append(behaviorScript);
            }
        }
        return script.length() > 0 ? script.toString() : null;
    }
}
