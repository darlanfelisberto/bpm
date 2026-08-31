package br.edu.iffar.box.component.menu;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Visual divider line or titled section separator inside a vertical menu.
 */
@FacesComponent(
        value = MenuSeparator.COMPONENT_TYPE,
        createTag = true,
        tagName = "separator",
        namespace = "http://iffar.edu.br/box")
public class MenuSeparator extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.MenuSeparator";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Menu";

    public MenuSeparator() {
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
        String label = getLabel();
        String styleClass = getStyleClass();
        String style = getStyle();

        writer.startElement("li", this);
        StringBuilder liClass = new StringBuilder("box-menu-separator");
        if (label != null && !label.isBlank()) {
            liClass.append(" box-menu-separator-with-label");
        }
        if (styleClass != null && !styleClass.isBlank()) {
            liClass.append(" ").append(styleClass.trim());
        }
        writer.writeAttribute("class", liClass.toString(), null);
        writer.writeAttribute("role", "separator", null);
        if (style != null && !style.isBlank()) {
            writer.writeAttribute("style", style, null);
        }

        if (label != null && !label.isBlank()) {
            writer.startElement("span", this);
            writer.writeAttribute("class", "box-menu-separator-label", null);
            writer.writeText(label, null);
            writer.endElement("span");
        }

        writer.endElement("li");
    }
}
