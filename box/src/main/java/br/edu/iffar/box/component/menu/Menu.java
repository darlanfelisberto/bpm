package br.edu.iffar.box.component.menu;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Vertical navigation menu container for statically declared items in Facelets XML.
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <b:menu header="Navigation">
 *          <b:menuitem value="Home" outcome="/index" icon="bi bi-house"/>
 *          <b:submenu label="Processes" icon="bi bi-folder">
 *              <b:menuitem value="List" outcome="/macroprocessos/list" icon="bi bi-list-task" badge="3"/>
 *              <b:menuitem value="New" outcome="/macroprocessos/novo" icon="bi bi-plus-circle"/>
 *          </b:submenu>
 *          <b:separator/>
 *          <b:menuitem value="External" url="https://example.com" target="_blank" icon="bi bi-box-arrow-up-right"/>
 *      </b:menu>
 */
@FacesComponent(
        value = Menu.COMPONENT_TYPE,
        createTag = true,
        tagName = "menu",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "box.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "menu/menu.js", target = "head")
})
public class Menu extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Menu";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Menu";

    public Menu() {
        setRendererType(null);
    }

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
        String clientId = getClientId(context);
        String styleClass = getStyleClass();
        String style = getStyle();

        writer.startElement("nav", this);
        writer.writeAttribute("id", clientId, "id");
        StringBuilder navClass = new StringBuilder("box-menu");
        if (styleClass != null && !styleClass.isBlank()) {
            navClass.append(" ").append(styleClass.trim());
        }
        writer.writeAttribute("class", navClass.toString(), null);
        if (style != null && !style.isBlank()) {
            writer.writeAttribute("style", style, null);
        }

        UIComponent headerFacet = getFacet("header");
        String headerText = getHeader();
        if (headerFacet != null || (headerText != null && !headerText.isBlank())) {
            writer.startElement("div", this);
            writer.writeAttribute("class", "box-menu-header", null);
            if (headerFacet != null) {
                headerFacet.encodeAll(context);
            } else {
                writer.writeText(headerText, "header");
            }
            writer.endElement("div");
        }

        writer.startElement("ul", this);
        writer.writeAttribute("class", "box-menu-list", null);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.endElement("ul");
        writer.endElement("nav");
    }
}
