package br.edu.iffar.box.component.panel;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Panel with an optional title, equivalent to p:panel (only with the
 * "header" attribute). Native component (self-rendering UIComponent), not
 * a composite: no separate Renderer, no *.taglib.xml — the tag is
 * generated at runtime by createTag/namespace/tagName below.
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

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", this);
        writer.writeAttribute("id", getClientId(context), "id");
        writer.writeAttribute("class", "card", null);

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
