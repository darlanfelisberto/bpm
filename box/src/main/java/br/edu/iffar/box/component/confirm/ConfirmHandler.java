package br.edu.iffar.box.component;

import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import java.io.IOException;

/**
 * Tag handler explícito para &lt;b:confirm&gt; — o BehaviorHandler padrão do
 * JSF (usado quando um &lt;behavior&gt; do taglib.xml não declara
 * handler-class próprio) resolve o atributo "mensagem" avaliando-o uma única
 * vez no build da view, o que quebra dentro de h:dataTable/ui:repeat (a
 * expressão é avaliada antes do var da linha existir). Aqui a
 * ValueExpression é capturada explicitamente via TagAttribute e guardada no
 * Confirm sem avaliar, para ser resolvida de novo a cada linha, na hora do
 * getScript().
 */
public class ConfirmHandler extends TagHandler {

    private final TagAttribute mensagem;

    public ConfirmHandler(TagConfig config) {
        super(config);
        this.mensagem = getAttribute("mensagem");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (!ComponentHandler.isNew(parent)) {
            return;
        }
        if (!(parent instanceof ClientBehaviorHolder)) {
            throw new IllegalStateException(
                    "b:confirm só pode ser usado dentro de um componente que aceite client behaviors "
                            + "(h:commandLink, h:commandButton, ...)");
        }

        FacesContext facesContext = ctx.getFacesContext();
        Application application = facesContext.getApplication();
        Confirm behavior = (Confirm) application.createBehavior(Confirm.BEHAVIOR_ID);

        if (mensagem != null) {
            ValueExpression expressao = mensagem.getValueExpression(ctx, String.class);
            behavior.setValueExpression("mensagem", expressao);
        }

        ClientBehaviorHolder holder = (ClientBehaviorHolder) parent;
        holder.addClientBehavior(holder.getDefaultEventName(), behavior);
    }
}
