package br.edu.iffar.box.component.confirm;

import jakarta.el.ValueExpression;
import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.behavior.ClientBehaviorBase;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.context.FacesContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Asks the user for confirmation before firing the parent component's
 * action — a small popup near the button, in the style of PrimeFaces'
 * p:confirm, without the browser's blocking confirm(). Native ClientBehavior
 * (not composite), nestable inside any ClientBehaviorHolder (h:commandLink,
 * h:commandButton, ...) the same way as f:ajax; no need for a component
 * variant per button type. Registered via META-INF/box.taglib.xml (behaviors
 * don't have the createTag shortcut that @FacesComponent has).
 *
 * "message" holds the ValueExpression (not the already-resolved value) and
 * only evaluates it when building the script, so it works correctly inside
 * h:dataTable/ui:repeat — the parent component is reused for every row, and
 * this is the standard JSF way (the same one used by AjaxBehavior/f:ajax) to
 * re-evaluate the expression on each row instead of fixing the value of the
 * first one.
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <h:commandLink action="#{bean.excluir}">
 *          ...
 *          <b:confirm message="Delete X?"/>
 *          <f:ajax render=":form"/>
 *      </h:commandLink>
 */
@FacesBehavior(value = Confirm.BEHAVIOR_ID)
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "confirm/confirm.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "confirm/confirm.js", target = "head")
})
public class Confirm extends ClientBehaviorBase {

    public static final String BEHAVIOR_ID = "br.edu.iffar.box.Confirm";

    private final Map<String, ValueExpression> bindings = new HashMap<>();
    private String messageLiteral;

    /** Empty when not configured - confirm.js falls back to a translated default message in that case. */
    public String getMessage() {
        ValueExpression expression = bindings.get("message");
        if (expression != null) {
            Object value = expression.getValue(FacesContext.getCurrentInstance().getELContext());
            return value != null ? value.toString() : "";
        }
        return messageLiteral != null ? messageLiteral : "";
    }

    public void setMessage(String message) {
        this.messageLiteral = message;
    }

    public ValueExpression getValueExpression(String name) {
        return bindings.get(name);
    }

    public void setValueExpression(String name, ValueExpression binding) {
        bindings.put(name, binding);
    }

    @Override
    public String getScript(ClientBehaviorContext behaviorContext) {
        return "return window.boxConfirm(this, event, '" + escapeJavaScript(getMessage()) + "');";
    }

    private static String escapeJavaScript(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ")
                .replace("\r", "");
    }
}
