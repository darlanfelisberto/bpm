package br.edu.iffar.box.component.growl;

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
 * FacesMessages rendered as floating toasts, equivalent to PrimeFaces'
 * p:growl. Native component (UIComponentBase, not composite): reads
 * FacesContext.getMessageList() on every render and hands the result off
 * to growl.js to stack as toasts - like any other message renderer
 * (h:messages, p:growl), it needs to be in the "render" of the
 * f:ajax/h:commandButton that added the message for it to show up.
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <b:growl/>
 *      <b:growl for="formCadastro" life="5000" sticky="false"/>
 */
@FacesComponent(
        value = Growl.COMPONENT_TYPE,
        createTag = true,
        tagName = "growl",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "box.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "growl/growl.js", target = "head")
})
public class Growl extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Growl";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Growl";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /** ClientId whose messages to show - null (default) shows all, global and from any component. */
    public String getFor() {
        return (String) getStateHelper().eval("for");
    }

    public void setFor(String forClientId) {
        getStateHelper().put("for", forClientId);
    }

    /** Time in milliseconds until the toast disappears on its own. Default 3000. */
    public int getLife() {
        Integer life = (Integer) getStateHelper().eval("life");
        return life != null ? life : 3000;
    }

    public void setLife(int life) {
        getStateHelper().put("life", life);
    }

    /** If true, the toast stays until the user closes it manually (ignores "life"). Default false. */
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

        // Same as box-schedule-eventos (Schedule.java): application/json
        // script, non-executable, with "<" escaped so a summary/detail
        // with "</script>" doesn't leak HTML into the rest of the page.
        writer.startElement("script", this);
        writer.writeAttribute("type", "application/json", null);
        writer.writeAttribute("class", "box-growl-messages", null);
        writer.write(messagesAsJson(context).replace("<", "\\u003C"));
        writer.endElement("script");
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div");
    }

    private String messagesAsJson(FacesContext context) {
        JsonArrayBuilder array = Json.createArrayBuilder();
        String forClientId = getFor();
        Iterator<FacesMessage> messages = forClientId != null
                ? context.getMessages(forClientId)
                : context.getMessages();
        while (messages.hasNext()) {
            FacesMessage message = messages.next();
            JsonObjectBuilder object = Json.createObjectBuilder();
            object.add("severity", severityAsText(message.getSeverity()));
            object.add("summary", message.getSummary() != null ? message.getSummary() : "");
            object.add("detail", message.getDetail() != null ? message.getDetail() : "");
            array.add(object);
            // Marks as rendered, otherwise MyFaces (RenderResponseExecutor) logs
            // "unhandled FacesMessages" at the end of RENDER_RESPONSE - the same
            // flag h:message/h:messages set when rendering.
            message.rendered();
        }
        return array.build().toString();
    }

    private String severityAsText(FacesMessage.Severity severity) {
        if (severity == FacesMessage.SEVERITY_WARN) {
            return "warn";
        } else if (severity == FacesMessage.SEVERITY_ERROR) {
            return "error";
        } else if (severity == FacesMessage.SEVERITY_FATAL) {
            return "fatal";
        }
        return "info";
    }
}
