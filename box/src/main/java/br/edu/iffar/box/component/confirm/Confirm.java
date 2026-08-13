package br.edu.iffar.box.component;

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
 * Pede confirmação do usuário antes de disparar a ação do componente pai —
 * um popup pequeno perto do botão, no estilo p:confirm do PrimeFaces, sem o
 * confirm() bloqueante do navegador. ClientBehavior nativo (não composite),
 * aninhável em qualquer ClientBehaviorHolder (h:commandLink,
 * h:commandButton, ...) do mesmo jeito que f:ajax; não precisa de uma
 * variante de componente por tipo de botão. Registrado via
 * META-INF/box.taglib.xml (behaviors não têm o atalho createTag que
 * @FacesComponent tem).
 *
 * "mensagem" guarda a ValueExpression (não o valor já resolvido) e só
 * avalia na hora de montar o script, para funcionar corretamente dentro de
 * h:dataTable/ui:repeat — o componente pai é reaproveitado a cada linha, e
 * essa é a forma padrão do JSF (a mesma usada por AjaxBehavior/f:ajax) de
 * reavaliar a expressão em cada linha em vez de fixar o valor da primeira.
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <h:commandLink action="#{bean.excluir}">
 *          ...
 *          <b:confirm mensagem="Excluir X?"/>
 *          <f:ajax render=":form"/>
 *      </h:commandLink>
 */
@FacesBehavior(value = Confirm.BEHAVIOR_ID)
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "confirmar.css", target = "head"),
        @ResourceDependency(library = "box", name = "confirmar.js", target = "head")
})
public class Confirm extends ClientBehaviorBase {

    public static final String BEHAVIOR_ID = "br.edu.iffar.box.Confirm";

    private static final String MENSAGEM_PADRAO = "Tem certeza?";

    private final Map<String, ValueExpression> bindings = new HashMap<>();
    private String mensagemLiteral;

    public String getMensagem() {
        ValueExpression expressao = bindings.get("mensagem");
        if (expressao != null) {
            Object valor = expressao.getValue(FacesContext.getCurrentInstance().getELContext());
            return valor != null ? valor.toString() : MENSAGEM_PADRAO;
        }
        return mensagemLiteral != null ? mensagemLiteral : MENSAGEM_PADRAO;
    }

    public void setMensagem(String mensagem) {
        this.mensagemLiteral = mensagem;
    }

    public ValueExpression getValueExpression(String name) {
        return bindings.get(name);
    }

    public void setValueExpression(String name, ValueExpression binding) {
        bindings.put(name, binding);
    }

    @Override
    public String getScript(ClientBehaviorContext behaviorContext) {
        return "return window.boxConfirmar(this, event, '" + escapeJavaScript(getMensagem()) + "');";
    }

    private static String escapeJavaScript(String valor) {
        return valor
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ")
                .replace("\r", "");
    }
}
