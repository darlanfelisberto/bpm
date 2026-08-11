package br.edu.iffar.box.component;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.html.HtmlCommandLink;

/**
 * Substituto de h:commandLink que pede confirmação do usuário antes de
 * disparar a ação — não com o confirm() nativo do navegador, mas um popup
 * pequeno posicionado perto do link (acima ou abaixo, conforme o espaço
 * disponível), no estilo p:confirm do PrimeFaces. Componente nativo: estende
 * HtmlCommandLink diretamente, reaproveitando o renderer e o encadeamento
 * com f:ajax padrão do JSF — só sobrescreve o onclick. O JS/CSS do popup
 * (confirmar.js/confirmar.css) é incluído automaticamente pelo
 * @ResourceDependency sempre que o componente é usado, sem precisar declarar
 * nada na página.
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <b:confirmarLink mensagem="Excluir X?" action="#{bean.excluir}">
 *          ...
 *          <f:ajax render=":form"/>
 *      </b:confirmarLink>
 */
@FacesComponent(
        value = ConfirmarLink.COMPONENT_TYPE,
        createTag = true,
        tagName = "confirmarLink",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "confirmar.css", target = "head"),
        @ResourceDependency(library = "box", name = "confirmar.js", target = "head")
})
public class ConfirmarLink extends HtmlCommandLink {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.ConfirmarLink";

    private static final String MENSAGEM_PADRAO = "Tem certeza?";

    public String getMensagem() {
        Object valor = getStateHelper().eval("mensagem");
        return valor != null ? valor.toString() : MENSAGEM_PADRAO;
    }

    public void setMensagem(String mensagem) {
        getStateHelper().put("mensagem", mensagem);
    }

    @Override
    public String getOnclick() {
        String script = "return window.boxConfirmar(this, event, '" + escapeJavaScript(getMensagem()) + "');";
        String existente = super.getOnclick();
        return existente != null ? script + existente : script;
    }

    private static String escapeJavaScript(String valor) {
        return valor
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ")
                .replace("\r", "");
    }
}
