package br.edu.iffar.box.component.confirm;

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
 * Explicit tag handler for &lt;b:confirm&gt; — JSF's default BehaviorHandler
 * (used when a &lt;behavior&gt; in taglib.xml doesn't declare its own
 * handler-class) resolves the "message" attribute by evaluating it exactly
 * once during view build, which breaks inside h:dataTable/ui:repeat (the
 * expression is evaluated before the row's var exists). Here the
 * ValueExpression is captured explicitly via TagAttribute and stored on
 * Confirm without evaluating it, to be resolved again on each row, when
 * getScript() runs.
 */
public class ConfirmHandler extends TagHandler {

    private final TagAttribute message;

    public ConfirmHandler(TagConfig config) {
        super(config);
        this.message = getAttribute("message");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (!ComponentHandler.isNew(parent)) {
            return;
        }
        if (!(parent instanceof ClientBehaviorHolder)) {
            throw new IllegalStateException(
                    "b:confirm can only be used inside a component that accepts client behaviors "
                            + "(h:commandLink, h:commandButton, ...)");
        }

        FacesContext facesContext = ctx.getFacesContext();
        Application application = facesContext.getApplication();
        Confirm behavior = (Confirm) application.createBehavior(Confirm.BEHAVIOR_ID);

        if (message != null) {
            ValueExpression expression = message.getValueExpression(ctx, String.class);
            behavior.setValueExpression("message", expression);
        }

        ClientBehaviorHolder holder = (ClientBehaviorHolder) parent;
        holder.addClientBehavior(holder.getDefaultEventName(), behavior);
    }
}
