package br.edu.iffar.box.component.question;

import jakarta.el.ValueExpression;
import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.*;

/**
 * Question component capable of rendering descriptive (multi-line textarea)
 * or multiple choice (radio button list) questions from a data model or direct attributes.
 *
 * Usage:
 * <pre>
 *   &lt;!-- Model-driven --&gt;
 *   &lt;b:question model="#{q}" value="#{bean.answers[q.id]}" /&gt;
 *
 *   &lt;!-- Declarative Descriptive --&gt;
 *   &lt;b:question prompt="Describe your feedback:"
 *               type="DESCRIPTIVE"
 *               value="#{bean.feedback}"
 *               required="true" /&gt;
 *
 *   &lt;!-- Declarative Choice --&gt;
 *   &lt;b:question prompt="Select priority:"
 *               type="CHOICE"
 *               options="#{bean.priorityOptions}"
 *               value="#{bean.selectedPriority}" /&gt;
 * </pre>
 */
@FacesComponent(
        value = Question.COMPONENT_TYPE,
        createTag = true,
        tagName = "question",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "box.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head")
})
public class Question extends UIInput implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Question";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Question";

    private static final List<String> EVENT_NAMES =
            Collections.unmodifiableList(List.of("change", "valueChange", "click"));

    private final Map<String, List<ClientBehavior>> behaviors = new HashMap<>();

    public Question() {
        setRendererType(null);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return "valueChange";
    }

    @Override
    public Map<String, List<ClientBehavior>> getClientBehaviors() {
        return Collections.unmodifiableMap(behaviors);
    }

    @Override
    public void addClientBehavior(String eventName, ClientBehavior behavior) {
        behaviors.computeIfAbsent(eventName, k -> new ArrayList<>()).add(behavior);
    }

    // --- Attributes ---

    public QuestionModel getModel() {
        return (QuestionModel) getStateHelper().eval("model");
    }

    public void setModel(QuestionModel model) {
        getStateHelper().put("model", model);
    }

    public String getPrompt() {
        QuestionModel model = getModel();
        return (String) getStateHelper().eval("prompt", model != null ? model.getPrompt() : null);
    }

    public void setPrompt(String prompt) {
        getStateHelper().put("prompt", prompt);
    }

    public QuestionType getType() {
        QuestionModel model = getModel();
        Object val = getStateHelper().eval("type", model != null ? model.getType() : null);
        return QuestionType.parse(val);
    }

    public void setType(QuestionType type) {
        getStateHelper().put("type", type);
    }

    @Override
    public boolean isRequired() {
        QuestionModel model = getModel();
        Object val = getStateHelper().eval("required", model != null ? model.isRequired() : false);
        return Boolean.TRUE.equals(val);
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
        getStateHelper().put("required", required);
    }

    public String getDescription() {
        QuestionModel model = getModel();
        return (String) getStateHelper().eval("description", model != null ? model.getDescription() : null);
    }

    public void setDescription(String description) {
        getStateHelper().put("description", description);
    }

    public Object getNumber() {
        return getStateHelper().eval("number");
    }

    public void setNumber(Object number) {
        getStateHelper().put("number", number);
    }

    public String getPlaceholder() {
        return (String) getStateHelper().eval("placeholder");
    }

    public void setPlaceholder(String placeholder) {
        getStateHelper().put("placeholder", placeholder);
    }

    public int getRows() {
        Object val = getStateHelper().eval("rows", 3);
        if (val instanceof Number n) {
            return n.intValue();
        }
        return 3;
    }

    public void setRows(int rows) {
        getStateHelper().put("rows", rows);
    }

    public Integer getCols() {
        Object val = getStateHelper().eval("cols");
        if (val instanceof Number n) {
            return n.intValue();
        }
        return null;
    }

    public void setCols(Integer cols) {
        getStateHelper().put("cols", cols);
    }

    public boolean isDisabled() {
        return Boolean.TRUE.equals(getStateHelper().eval("disabled", false));
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put("disabled", disabled);
    }

    public boolean isReadonly() {
        return Boolean.TRUE.equals(getStateHelper().eval("readonly", false));
    }

    public void setReadonly(boolean readonly) {
        getStateHelper().put("readonly", readonly);
    }

    public String getStyle() {
        return (String) getStateHelper().eval("style");
    }

    public void setStyle(String style) {
        getStateHelper().put("style", style);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval("styleClass");
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put("styleClass", styleClass);
    }

    public Object getOptions() {
        QuestionModel model = getModel();
        return getStateHelper().eval("options", model != null ? model.getOptions() : null);
    }

    public void setOptions(Object options) {
        getStateHelper().put("options", options);
    }

    @SuppressWarnings("unchecked")
    public List<?> resolveOptionsList() {
        Object val = getOptions();
        if (val instanceof List<?> list) {
            return list;
        } else if (val instanceof Collection<?> col) {
            return new ArrayList<>(col);
        } else if (val instanceof Object[] arr) {
            return Arrays.asList(arr);
        }
        return List.of();
    }

    // --- Decode ---

    @Override
    public void decode(FacesContext context) {
        if (!isRendered() || isDisabled() || isReadonly()) {
            return;
        }

        String clientId = getClientId(context);
        Map<String, String> requestParams = context.getExternalContext().getRequestParameterMap();

        if (requestParams.containsKey(clientId)) {
            String submitted = requestParams.get(clientId);
            setSubmittedValue(submitted);
        }

        // Decode ClientBehavior if source matches this component
        String source = requestParams.get("jakarta.faces.source");
        String behaviorEvent = requestParams.get("jakarta.faces.behavior.event");

        if (clientId.equals(source) && behaviorEvent != null) {
            List<ClientBehavior> behaviorList = behaviors.get(behaviorEvent);
            if (behaviorList != null) {
                for (ClientBehavior behavior : behaviorList) {
                    behavior.decode(context, this);
                }
            }
        }
    }

    // --- Encode ---

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        String clientId = getClientId(context);
        QuestionType type = getType();
        boolean disabled = isDisabled();
        boolean readonly = isReadonly();
        boolean required = isRequired();
        boolean invalid = !isValid();

        StringBuilder css = new StringBuilder("box-question");
        css.append(" box-question-").append(type.name().toLowerCase(Locale.ROOT));
        if (disabled) {
            css.append(" is-disabled");
        }
        if (readonly) {
            css.append(" is-readonly");
        }
        if (invalid) {
            css.append(" is-invalid");
        }
        String customClass = getStyleClass();
        if (customClass != null && !customClass.isBlank()) {
            css.append(" ").append(customClass);
        }

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", css.toString(), "styleClass");
        writer.writeAttribute("data-type", type.name().toLowerCase(Locale.ROOT), null);

        String style = getStyle();
        if (style != null && !style.isBlank()) {
            writer.writeAttribute("style", style, "style");
        }

        // Question Header
        encodeHeader(context, writer);

        // Question Body
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-question-body", null);

        if (type == QuestionType.DESCRIPTIVE) {
            encodeDescriptive(context, writer, clientId, disabled, readonly);
        } else {
            encodeChoice(context, writer, clientId, disabled, readonly);
        }

        writer.endElement("div"); // .box-question-body
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div"); // .box-question
    }

    private void encodeHeader(FacesContext context, ResponseWriter writer) throws IOException {
        String prompt = getPrompt();
        Object number = getNumber();
        String description = getDescription();
        boolean required = isRequired();

        if (prompt == null && number == null && description == null) {
            return;
        }

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-question-header", null);

        if (number != null && !String.valueOf(number).isBlank()) {
            writer.startElement("span", this);
            writer.writeAttribute("class", "box-question-number", null);
            writer.writeText(String.valueOf(number) + (String.valueOf(number).endsWith(".") ? "" : "."), null);
            writer.endElement("span");
        }

        if (prompt != null && !prompt.isBlank()) {
            writer.startElement("label", this);
            writer.writeAttribute("class", "box-question-prompt", null);
            writer.writeText(prompt, "prompt");

            if (required) {
                writer.startElement("span", this);
                writer.writeAttribute("class", "box-question-required", null);
                writer.writeAttribute("aria-hidden", "true", null);
                writer.writeText(" *", null);
                writer.endElement("span");
            }
            writer.endElement("label");
        }

        if (description != null && !description.isBlank()) {
            writer.startElement("div", this);
            writer.writeAttribute("class", "box-question-description", null);
            writer.writeText(description, "description");
            writer.endElement("div");
        }

        writer.endElement("div"); // .box-question-header
    }

    private void encodeDescriptive(FacesContext context, ResponseWriter writer,
                                   String clientId, boolean disabled, boolean readonly) throws IOException {
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-question-descriptive", null);

        writer.startElement("textarea", this);
        String inputId = clientId + "_input";
        writer.writeAttribute("id", inputId, "id");
        writer.writeAttribute("name", clientId, null);
        writer.writeAttribute("class", "box-question-textarea", null);
        writer.writeAttribute("rows", String.valueOf(getRows()), "rows");

        Integer cols = getCols();
        if (cols != null) {
            writer.writeAttribute("cols", String.valueOf(cols), "cols");
        }

        String placeholder = getPlaceholder();
        if (placeholder != null && !placeholder.isBlank()) {
            writer.writeAttribute("placeholder", placeholder, "placeholder");
        }

        if (disabled) {
            writer.writeAttribute("disabled", "disabled", "disabled");
        }
        if (readonly) {
            writer.writeAttribute("readonly", "readonly", "readonly");
        }
        if (isRequired()) {
            writer.writeAttribute("aria-required", "true", null);
        }

        String behaviorScript = buildBehaviorScript(context, clientId, "valueChange", "change");
        if (behaviorScript != null) {
            writer.writeAttribute("onchange", behaviorScript, null);
        }

        Object currentValue = getCurrentValue();
        if (currentValue != null) {
            writer.writeText(String.valueOf(currentValue), "value");
        }

        writer.endElement("textarea");
        writer.endElement("div");
    }

    private void encodeChoice(FacesContext context, ResponseWriter writer,
                             String clientId, boolean disabled, boolean readonly) throws IOException {
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-question-options", null);

        List<?> optionsList = resolveOptionsList();
        Object currentValue = getCurrentValue();
        String currentStr = currentValue != null ? String.valueOf(currentValue) : null;
        String behaviorScript = buildBehaviorScript(context, clientId, "valueChange", "change", "click");

        int index = 0;
        for (Object item : optionsList) {
            String optValue;
            String optLabel;
            String optDesc = null;
            boolean optDisabled = disabled;

            if (item instanceof QuestionOption opt) {
                optValue = opt.getValue() != null ? String.valueOf(opt.getValue()) : "";
                optLabel = opt.getLabel();
                optDesc = opt.getDescription();
                optDisabled = disabled || opt.isDisabled();
            } else {
                optValue = item != null ? String.valueOf(item) : "";
                optLabel = optValue;
            }

            boolean checked = currentStr != null && currentStr.equals(optValue);
            String optionId = clientId + "_opt" + index;

            writer.startElement("label", this);
            StringBuilder optionClass = new StringBuilder("box-question-option");
            if (checked) {
                optionClass.append(" is-selected");
            }
            if (optDisabled) {
                optionClass.append(" is-disabled");
            }
            writer.writeAttribute("class", optionClass.toString(), null);
            writer.writeAttribute("for", optionId, null);

            writer.startElement("input", this);
            writer.writeAttribute("type", "radio", null);
            writer.writeAttribute("id", optionId, "id");
            writer.writeAttribute("name", clientId, null);
            writer.writeAttribute("value", optValue, null);
            if (checked) {
                writer.writeAttribute("checked", "checked", null);
            }
            if (optDisabled) {
                writer.writeAttribute("disabled", "disabled", null);
            }
            if (readonly) {
                writer.writeAttribute("readonly", "readonly", null);
            }
            if (isRequired()) {
                writer.writeAttribute("aria-required", "true", null);
            }
            if (behaviorScript != null && !optDisabled && !readonly) {
                writer.writeAttribute("onchange", behaviorScript, null);
            }
            writer.endElement("input");

            writer.startElement("span", this);
            writer.writeAttribute("class", "box-question-option-content", null);

            writer.startElement("span", this);
            writer.writeAttribute("class", "box-question-option-label", null);
            writer.writeText(optLabel != null ? optLabel : optValue, null);
            writer.endElement("span");

            if (optDesc != null && !optDesc.isBlank()) {
                writer.startElement("span", this);
                writer.writeAttribute("class", "box-question-option-desc", null);
                writer.writeText(optDesc, null);
                writer.endElement("span");
            }

            writer.endElement("span"); // .box-question-option-content
            writer.endElement("label"); // .box-question-option

            index++;
        }

        writer.endElement("div"); // .box-question-options
    }

    private Object getCurrentValue() {
        Object submitted = getSubmittedValue();
        if (submitted != null) {
            return submitted;
        }
        return getValue();
    }

    private String buildBehaviorScript(FacesContext context, String clientId, String... eventNames) {
        for (String eventName : eventNames) {
            List<ClientBehavior> list = behaviors.get(eventName);
            if (list != null && !list.isEmpty()) {
                ClientBehaviorContext behaviorContext = ClientBehaviorContext.createClientBehaviorContext(
                        context, this, eventName, clientId, null);
                StringBuilder sb = new StringBuilder();
                for (ClientBehavior b : list) {
                    String script = b.getScript(behaviorContext);
                    if (script != null && !script.isBlank()) {
                        sb.append(script).append(";");
                    }
                }
                if (sb.length() > 0) {
                    return sb.toString();
                }
            }
        }
        return null;
    }
}
