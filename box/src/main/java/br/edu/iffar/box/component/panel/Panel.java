package br.edu.iffar.box.component;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Quadro com título opcional, equivalente ao p:panel (só com o atributo
 * "header"). Componente nativo (UIComponent auto-renderizável), não
 * composite: sem Renderer separado, sem *.taglib.xml — a tag é gerada em
 * tempo de execução por createTag/namespace/tagName abaixo.
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <b:panel header="Novo instrumento">...</b:panel>
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
