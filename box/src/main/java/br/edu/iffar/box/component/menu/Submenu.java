package br.edu.iffar.box.component.menu;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Group of menu items with a header and optional collapse/expand behavior.
 */
@FacesComponent(
        value = Submenu.COMPONENT_TYPE,
        createTag = true,
        tagName = "submenu",
        namespace = "http://iffar.edu.br/box")
public class Submenu extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Submenu";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Menu";

    public Submenu() {
        setRendererType(null);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getLabel() {
        return (String) getStateHelper().eval("label");
    }

    public void setLabel(String label) {
        getStateHelper().put("label", label);
    }

    public String getIcon() {
        return (String) getStateHelper().eval("icon");
    }

    public void setIcon(String icon) {
        getStateHelper().put("icon", icon);
    }

    public boolean isExpanded() {
        Boolean expanded = (Boolean) getStateHelper().eval("expanded");
        return expanded == null || expanded;
    }

    public void setExpanded(boolean expanded) {
        getStateHelper().put("expanded", expanded);
    }

    public boolean isCollapsible() {
        Boolean collapsible = (Boolean) getStateHelper().eval("collapsible");
        return collapsible != null && collapsible;
    }

    public void setCollapsible(boolean collapsible) {
        getStateHelper().put("collapsible", collapsible);
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

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        boolean expanded = isExpanded();
        boolean collapsible = isCollapsible();
        String styleClass = getStyleClass();
        String style = getStyle();

        writer.startElement("li", this);
        StringBuilder liClass = new StringBuilder("box-menu-submenu");
        if (!expanded) {
            liClass.append(" box-menu-collapsed");
        }
        if (collapsible) {
            liClass.append(" box-menu-collapsible");
        }
        if (styleClass != null && !styleClass.isBlank()) {
            liClass.append(" ").append(styleClass.trim());
        }
        writer.writeAttribute("class", liClass.toString(), null);
        if (style != null && !style.isBlank()) {
            writer.writeAttribute("style", style, null);
        }

        String label = getLabel();
        String icon = getIcon();
        if ((label != null && !label.isBlank()) || (icon != null && !icon.isBlank())) {
            writer.startElement("div", this);
            writer.writeAttribute("class", "box-menu-submenu-header", null);
            if (collapsible) {
                writer.writeAttribute("role", "button", null);
                writer.writeAttribute("tabindex", "0", null);
                writer.writeAttribute("aria-expanded", String.valueOf(expanded), null);
            }

            writer.startElement("div", this);
            writer.writeAttribute("class", "box-menu-submenu-header-content", null);

            if (icon != null && !icon.isBlank()) {
                writer.startElement("i", this);
                writer.writeAttribute("class", icon + " box-menu-icon", null);
                writer.writeAttribute("aria-hidden", "true", null);
                writer.endElement("i");
            }

            if (label != null && !label.isBlank()) {
                writer.startElement("span", this);
                writer.writeAttribute("class", "box-menu-label", null);
                writer.writeText(label, null);
                writer.endElement("span");
            }

            writer.endElement("div");

            if (collapsible) {
                writer.startElement("span", this);
                writer.writeAttribute("class", "box-menu-toggle-icon", null);
                writer.writeAttribute("aria-hidden", "true", null);
                writer.endElement("span");
            }

            writer.endElement("div");
        }

        writer.startElement("ul", this);
        writer.writeAttribute("class", "box-menu-sublist", null);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.endElement("ul");
        writer.endElement("li");
    }
}
