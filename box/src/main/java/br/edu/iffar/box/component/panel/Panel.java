package br.edu.iffar.box.component.panel;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Panel with an optional title, equivalent to p:panel (only with the
 * "header" attribute).
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *        <b:panel header="New instrument">...</b:panel>
 */
@FacesComponent(
        value = Panel.COMPONENT_TYPE,
        createTag = true,
        tagName = "panel",
        namespace = "http://iffar.edu.br/box")
public class Panel extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Panel";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Panel";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getHeader() {
        return (String) getStateHelper().eval("header");
    }

    public void setHeader(String header) {
        getStateHelper().put("header", header);
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

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", this);
        writer.writeAttribute("id", getClientId(context), "id");

        String styleClass = getStyleClass();
        String clazz = styleClass != null && !styleClass.isBlank() ? "card " + styleClass.trim() : "card";
        writer.writeAttribute("class", clazz, null);

        String style = getStyle();
        if (style != null && !style.isBlank()) {
            writer.writeAttribute("style", style, null);
        }

        String header = getHeader();
        if (header != null && !header.isBlank()) {
            writer.startElement("h3", this);
            writer.writeText(header, "header");
            writer.endElement("h3");
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div");
    }
}
