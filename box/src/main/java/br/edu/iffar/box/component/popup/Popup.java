package br.edu.iffar.box.component.popup;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Modal popup (equivalent to p:dialog), based on the native &lt;dialog&gt;
 * element - the browser's own showModal()/close() take care of focus,
 * Esc and stacking (top layer), without reimplementing any of that in
 * JS. Opening and closing are client-side: call Box.popup.open('id') /
 * Box.popup.close('id') from an onclick, from another component, or
 * from the oncomplete of an f:ajax.
 *
 * Always client-side, with no state kept on the bean: opening is always
 * Box.popup.open('id'). If the bean needs to close the popup as a
 * reaction to an action (e.g., only close after saving without errors),
 * use FacesContext.getCurrentInstance().getPartialViewContext()
 * .getEvalScripts().add(...) inside the bean's own method - it's the
 * standard Jakarta Faces channel for making the client execute JS
 * alongside the ajax response (equivalent to PrimeFaces'
 * RequestContext.execute()), without needing a "visible"/"open"
 * property on the bean or including the popup in "render".
 *
 *      xmlns:b="http://iffar.edu.br/box"
 *      <b:popup id="dlg" header="Edit item">
 *          content here
 *          <f:facet name="footer">
 *              <h:commandButton value="Save" action="#{bean.save}">
 *                  <f:ajax execute="@form" render="..."/>
 *              </h:commandButton>
 *          </f:facet>
 *      </b:popup>
 *      <h:commandButton value="Open" onclick="Box.popup.open('dlg'); return false;"/>
 *
 * In the bean:
 *      public void save() {
 *          // ... persist ...
 *          FacesContext.getCurrentInstance().getPartialViewContext()
 *                  .getEvalScripts().add("Box.popup.close('dlg')");
 *      }
 */
@FacesComponent(
        value = Popup.COMPONENT_TYPE,
        createTag = true,
        tagName = "popup",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "popup/popup.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "popup/popup.js", target = "head")
})
public class Popup extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Popup";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Popup";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /** Title shown in the header. If omitted (and there is no "header" facet), the popup has no header. */
    public String getHeader() {
        return (String) getStateHelper().eval("header");
    }

    public void setHeader(String header) {
        getStateHelper().put("header", header);
    }

    /** If false, removes the close button and disables closing by clicking outside/Esc. Default true. */
    public boolean isClosable() {
        Boolean closable = (Boolean) getStateHelper().eval("closable");
        return closable == null || closable;
    }

    public void setClosable(boolean closable) {
        getStateHelper().put("closable", closable);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("dialog", this);
        writer.writeAttribute("id", getClientId(context), "id");
        writer.writeAttribute("class", "box-popup", null);
        writer.writeAttribute("data-box-popup-closable", String.valueOf(isClosable()), null);

        UIComponent header = getFacet("header");
        String headerText = getHeader();
        if (header != null || (headerText != null && !headerText.isBlank()) || isClosable()) {
            writer.startElement("div", this);
            writer.writeAttribute("class", "box-popup-header", null);

            if (header != null) {
                header.encodeAll(context);
            } else if (headerText != null && !headerText.isBlank()) {
                writer.startElement("h3", this);
                writer.writeText(headerText, "header");
                writer.endElement("h3");
            }

            if (isClosable()) {
                writer.startElement("button", this);
                writer.writeAttribute("type", "button", null);
                writer.writeAttribute("class", "box-popup-close", null);
                writer.writeAttribute("aria-label", "Close", null);
                writer.writeText("×", null);
                writer.endElement("button");
            }

            writer.endElement("div");
        }

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-popup-body", null);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.endElement("div");

        UIComponent footer = getFacet("footer");
        if (footer != null) {
            writer.startElement("div", this);
            writer.writeAttribute("class", "box-popup-footer", null);
            footer.encodeAll(context);
            writer.endElement("div");
        }

        writer.endElement("dialog");
    }
}
