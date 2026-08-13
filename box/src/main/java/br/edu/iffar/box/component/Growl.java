package br.edu.iffar.box.component;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;
import java.util.Iterator;

/**
 * Mensagens (FacesMessage) como toasts flutuantes, equivalente ao p:growl
 * do PrimeFaces. Componente nativo (UIComponentBase, não composite): lê
 * FacesContext.getMessageList() a cada renderização e entrega o resultado
 * pro growl.js empilhar como toasts - como qualquer outro exibidor de
 * mensagens (h:messages, p:growl), precisa estar no "render" do
 * f:ajax/h:commandButton que adicionou a mensagem pra ela aparecer.
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <b:growl/>
 *      <b:growl for="formCadastro" life="5000" sticky="false"/>
 */
@FacesComponent(
        value = Growl.COMPONENT_TYPE,
        createTag = true,
        tagName = "growl",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "growl.css", target = "head"),
        @ResourceDependency(library = "box", name = "box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "growl.js", target = "head")
})
public class Growl extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Growl";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Growl";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /** ClientId cujas mensagens exibir - null (padrão) mostra todas, globais e de qualquer componente. */
    public String getFor() {
        return (String) getStateHelper().eval("for");
    }

    public void setFor(String forClientId) {
        getStateHelper().put("for", forClientId);
    }

    /** Tempo em milissegundos até o toast sumir sozinho. Padrão 3000. */
    public int getLife() {
        Integer life = (Integer) getStateHelper().eval("life");
        return life != null ? life : 3000;
    }

    public void setLife(int life) {
        getStateHelper().put("life", life);
    }

    /** Se true, o toast fica até o usuário fechar manualmente (ignora "life"). Padrão false. */
    public boolean isSticky() {
        Boolean sticky = (Boolean) getStateHelper().eval("sticky");
        return sticky != null && sticky;
    }

    public void setSticky(boolean sticky) {
        getStateHelper().put("sticky", sticky);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", this);
        writer.writeAttribute("id", getClientId(context), "id");
        writer.writeAttribute("class", "box-growl", null);
        writer.writeAttribute("data-life", String.valueOf(getLife()), null);
        writer.writeAttribute("data-sticky", String.valueOf(isSticky()), null);

        // Igual ao box-schedule-eventos (Schedule.java): script
        // application/json, nao executavel, com "<" escapado pra um
        // resumo/detalhe com "</script>" nao vazar HTML pro resto da
        // pagina.
        writer.startElement("script", this);
        writer.writeAttribute("type", "application/json", null);
        writer.writeAttribute("class", "box-growl-mensagens", null);
        writer.write(mensagensComoJson(context).replace("<", "\\u003C"));
        writer.endElement("script");
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div");
    }

    private String mensagensComoJson(FacesContext context) {
        JsonArrayBuilder array = Json.createArrayBuilder();
        String forClientId = getFor();
        Iterator<FacesMessage> mensagens = forClientId != null
                ? context.getMessages(forClientId)
                : context.getMessages();
        while (mensagens.hasNext()) {
            FacesMessage mensagem = mensagens.next();
            JsonObjectBuilder objeto = Json.createObjectBuilder();
            objeto.add("severidade", severidadeComoTexto(mensagem.getSeverity()));
            objeto.add("resumo", mensagem.getSummary() != null ? mensagem.getSummary() : "");
            objeto.add("detalhe", mensagem.getDetail() != null ? mensagem.getDetail() : "");
            array.add(objeto);
            // Marca como exibida, senao o MyFaces (RenderResponseExecutor) loga
            // "unhandled FacesMessages" no final do RENDER_RESPONSE - o mesmo
            // flag que h:message/h:messages setam ao renderizar.
            mensagem.rendered();
        }
        return array.build().toString();
    }

    private String severidadeComoTexto(FacesMessage.Severity severidade) {
        if (severidade == FacesMessage.SEVERITY_WARN) {
            return "warn";
        } else if (severidade == FacesMessage.SEVERITY_ERROR) {
            return "error";
        } else if (severidade == FacesMessage.SEVERITY_FATAL) {
            return "fatal";
        }
        return "info";
    }
}
