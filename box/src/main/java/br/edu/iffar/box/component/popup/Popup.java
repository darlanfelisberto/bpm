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
 * Popup modal (equivalente ao p:dialog), baseado no elemento nativo
 * &lt;dialog&gt; - showModal()/close() do próprio browser cuidam de foco,
 * Esc e empilhamento (top layer), sem reimplementar nada disso em JS.
 * Abertura e fechamento são client-side: chame Box.popup.abrir('id') /
 * Box.popup.fechar('id') a partir de um onclick, de outro componente ou do
 * oncomplete de um f:ajax.
 *
 * Sempre client-side, sem estado nenhum guardado no bean: abrir é sempre
 * Box.popup.abrir('id'). Se o bean precisar fechar o popup como reação a
 * uma ação (ex.: só fechar depois de salvar sem erro), use
 * FacesContext.getCurrentInstance().getPartialViewContext()
 * .getEvalScripts().add(...) dentro do próprio método do bean - é o canal
 * padrão do Jakarta Faces pra mandar o cliente executar JS junto da
 * resposta ajax (equivalente ao RequestContext.execute() do PrimeFaces),
 * sem precisar de uma propriedade "visible"/"aberto" no bean nem incluir o
 * popup no "render".
 *
 *      xmlns:b="http://iffar.edu.br/box"
 *      <b:popup id="dlg" header="Editar item">
 *          conteúdo aqui
 *          <f:facet name="footer">
 *              <h:commandButton value="Salvar" action="#{bean.salvar}">
 *                  <f:ajax execute="@form" render="..."/>
 *              </h:commandButton>
 *          </f:facet>
 *      </b:popup>
 *      <h:commandButton value="Abrir" onclick="Box.popup.abrir('dlg'); return false;"/>
 *
 * No bean:
 *      public void salvar() {
 *          // ... persistir ...
 *          FacesContext.getCurrentInstance().getPartialViewContext()
 *                  .getEvalScripts().add("Box.popup.fechar('dlg')");
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

    /** Título exibido no cabeçalho. Omitido (e sem facet "header"), o popup fica sem cabeçalho. */
    public String getHeader() {
        return (String) getStateHelper().eval("header");
    }

    public void setHeader(String header) {
        getStateHelper().put("header", header);
    }

    /** Se false, remove o botão de fechar e desliga fechar por clique fora/Esc. Padrão true. */
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
        writer.writeAttribute("data-box-popup-fechavel", String.valueOf(isClosable()), null);

        UIComponent header = getFacet("header");
        String headerTexto = getHeader();
        if (header != null || (headerTexto != null && !headerTexto.isBlank()) || isClosable()) {
            writer.startElement("div", this);
            writer.writeAttribute("class", "box-popup-cabecalho", null);

            if (header != null) {
                header.encodeAll(context);
            } else if (headerTexto != null && !headerTexto.isBlank()) {
                writer.startElement("h3", this);
                writer.writeText(headerTexto, "header");
                writer.endElement("h3");
            }

            if (isClosable()) {
                writer.startElement("button", this);
                writer.writeAttribute("type", "button", null);
                writer.writeAttribute("class", "box-popup-fechar", null);
                writer.writeAttribute("aria-label", "Fechar", null);
                writer.writeText("×", null);
                writer.endElement("button");
            }

            writer.endElement("div");
        }

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-popup-corpo", null);
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
            writer.writeAttribute("class", "box-popup-rodape", null);
            footer.encodeAll(context);
            writer.endElement("div");
        }

        writer.endElement("dialog");
    }
}
