package br.edu.iffar.box.component.question;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
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
 * Supports rendering either a single question or a list/collection of questions
 * directly through the {@code model} attribute.
 *
 * Usage:
 * <pre>
 *   &lt;!-- Multiple Questions Model-driven (List / Collection) --&gt;
 *   &lt;b:question model="#{bean.questionList}" value="#{bean.answersMap}" /&gt;
 *
 *   &lt;!-- Single Question Model-driven --&gt;
 *   &lt;b:question model="#{q}" value="#{bean.answer}" /&gt;
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

    public Object getModel() {
        return getStateHelper().eval("model");
    }

    public void setModel(Object model) {
        getStateHelper().put("model", model);
    }

    public boolean isMultipleQuestions() {
        Object m = getModel();
        return m instanceof Collection<?> || m instanceof Object[];
    }

    @SuppressWarnings("unchecked")
    public List<QuestionModel> resolveQuestionList() {
        Object m = getModel();
        if (m instanceof List<?> list) {
            return (List<QuestionModel>) list;
        } else if (m instanceof Collection<?> col) {
            return new ArrayList<>((Collection<QuestionModel>) col);
        } else if (m instanceof Object[] arr) {
            List<QuestionModel> list = new ArrayList<>();
            for (Object item : arr) {
                if (item instanceof QuestionModel qm) {
                    list.add(qm);
                }
            }
            return list;
        } else if (m instanceof QuestionModel qm) {
            return List.of(qm);
        }
        return List.of();
    }

    public QuestionModel resolveSingleModel() {
        Object m = getModel();
        if (m instanceof QuestionModel qm) {
            return qm;
        }
        return null;
    }

    public String getPrompt() {
        QuestionModel model = resolveSingleModel();
        return (String) getStateHelper().eval("prompt", model != null ? model.getPrompt() : null);
    }

    public void setPrompt(String prompt) {
        getStateHelper().put("prompt", prompt);
    }

    public QuestionType getType() {
        QuestionModel model = resolveSingleModel();
        Object val = getStateHelper().eval("type", model != null ? model.getType() : null);
        return QuestionType.parse(val);
    }

    public void setType(QuestionType type) {
        getStateHelper().put("type", type);
    }

    @Override
    public boolean isRequired() {
        QuestionModel model = resolveSingleModel();
        Object val = getStateHelper().eval("required", model != null ? model.isRequired() : false);
        return Boolean.TRUE.equals(val);
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
        getStateHelper().put("required", required);
    }

    public String getDescription() {
        QuestionModel model = resolveSingleModel();
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
        QuestionModel model = resolveSingleModel();
        return getStateHelper().eval("options", model != null ? model.getOptions() : null);
    }

    public void setOptions(Object options) {
        getStateHelper().put("options", options);
    }

    @SuppressWarnings("unchecked")
    public List<?> resolveOptionsList(Object rawOptions) {
        if (rawOptions instanceof List<?> list) {
            return list;
        } else if (rawOptions instanceof Collection<?> col) {
            return new ArrayList<>(col);
        } else if (rawOptions instanceof Object[] arr) {
            return Arrays.asList(arr);
        }
        return List.of();
    }

    // --- Decode & Model Update ---

    @Override
    public void decode(FacesContext context) {
        if (!isRendered() || isDisabled() || isReadonly()) {
            return;
        }

        String clientId = getClientId(context);
        Map<String, String> requestParams = context.getExternalContext().getRequestParameterMap();

        if (isMultipleQuestions()) {
            List<QuestionModel> questions = resolveQuestionList();
            Map<Object, Object> submittedMap = new HashMap<>();
            boolean hasSubmission = false;

            for (int i = 0; i < questions.size(); i++) {
                QuestionModel q = questions.get(i);
                Object key = q.getId() != null ? q.getId() : i;
                String inputName = clientId + "_" + key;
                if (requestParams.containsKey(inputName)) {
                    submittedMap.put(key, requestParams.get(inputName));
                    hasSubmission = true;
                }
            }

            if (hasSubmission) {
                setSubmittedValue(submittedMap);
            }
        } else {
            if (requestParams.containsKey(clientId)) {
                String submitted = requestParams.get(clientId);
                setSubmittedValue(submitted);
            }
        }

        // Decode ClientBehavior if source matches this component
        String source = requestParams.get("jakarta.faces.source");
        String behaviorEvent = requestParams.get("jakarta.faces.behavior.event");

        if (source != null && (source.equals(clientId) || source.startsWith(clientId + "_")) && behaviorEvent != null) {
            List<ClientBehavior> behaviorList = behaviors.get(behaviorEvent);
            if (behaviorList != null) {
                for (ClientBehavior behavior : behaviorList) {
                    behavior.decode(context, this);
                }
            }
        }
    }

    @Override
    public void updateModel(FacesContext context) {
        if (!isMultipleQuestions()) {
            super.updateModel(context);
            return;
        }

        if (!isValid()) {
            return;
        }

        ValueExpression ve = getValueExpression("value");
        if (ve != null) {
            try {
                Object currentModelValue = ve.getValue(context.getELContext());
                Object submitted = getSubmittedValue();
                if (submitted instanceof Map<?, ?> submittedMap) {
                    if (currentModelValue instanceof Map map) {
                        map.putAll(submittedMap);
                        setValue(map);
                        setSubmittedValue(null);
                    } else {
                        ve.setValue(context.getELContext(), submittedMap);
                        setValue(submittedMap);
                        setSubmittedValue(null);
                    }
                }
            } catch (Exception e) {
                context.addMessage(getClientId(context),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Update error", e.getMessage()));
                setValid(false);
            }
        }
    }

    public Object getCurrentAnswer(Object questionKey) {
        Object submitted = getSubmittedValue();
        if (submitted instanceof Map<?, ?> map && map.containsKey(questionKey)) {
            return map.get(questionKey);
        }
        Object val = getValue();
        if (val instanceof Map<?, ?> map && map.containsKey(questionKey)) {
            return map.get(questionKey);
        }
        return null;
    }

    private Object getCurrentSingleValue() {
        Object submitted = getSubmittedValue();
        if (submitted != null) {
            return submitted;
        }
        return getValue();
    }

    // --- Encode ---

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        String clientId = getClientId(context);

        if (isMultipleQuestions()) {
            encodeMultipleQuestions(context, writer, clientId);
        } else {
            encodeSingleQuestion(context, writer, clientId, resolveSingleModel(), null, 0);
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        if (isMultipleQuestions()) {
            writer.endElement("div"); // .box-questions-container
        } else {
            writer.endElement("div"); // .box-question
        }
    }

    private void encodeMultipleQuestions(FacesContext context, ResponseWriter writer, String clientId) throws IOException {
        List<QuestionModel> questions = resolveQuestionList();
        StringBuilder containerClass = new StringBuilder("box-questions-container");
        String customClass = getStyleClass();
        if (customClass != null && !customClass.isBlank()) {
            containerClass.append(" ").append(customClass);
        }

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", containerClass.toString(), "styleClass");

        String style = getStyle();
        if (style != null && !style.isBlank()) {
            writer.writeAttribute("style", style, "style");
        }

        for (int i = 0; i < questions.size(); i++) {
            QuestionModel q = questions.get(i);
            Object key = q.getId() != null ? q.getId() : i;
            String subId = clientId + "_q" + i;
            encodeSingleQuestion(context, writer, subId, q, key, i + 1);
            writer.endElement("div"); // close .box-question for this item
        }
    }

    private void encodeSingleQuestion(FacesContext context, ResponseWriter writer,
                                      String elementId, QuestionModel model,
                                      Object questionKey, int defaultIndex) throws IOException {
        String mainClientId = getClientId(context);
        QuestionType type = model != null ? model.getType() : getType();
        boolean disabled = isDisabled();
        boolean readonly = isReadonly();
        boolean required = model != null ? model.isRequired() : isRequired();
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

        // Only add custom class on individual card when not rendering a list container
        if (!isMultipleQuestions()) {
            String customClass = getStyleClass();
            if (customClass != null && !customClass.isBlank()) {
                css.append(" ").append(customClass);
            }
        }

        writer.startElement("div", this);
        writer.writeAttribute("id", elementId, "id");
        writer.writeAttribute("class", css.toString(), "styleClass");
        writer.writeAttribute("data-type", type.name().toLowerCase(Locale.ROOT), null);

        if (!isMultipleQuestions()) {
            String style = getStyle();
            if (style != null && !style.isBlank()) {
                writer.writeAttribute("style", style, "style");
            }
        }

        // Question Header
        String prompt = model != null ? model.getPrompt() : getPrompt();
        Object number = getNumber();
        if (number == null && defaultIndex > 0) {
            number = defaultIndex;
        }
        String description = model != null ? model.getDescription() : getDescription();
        encodeHeader(writer, prompt, number, description, required);

        // Question Body
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-question-body", null);

        String inputName = isMultipleQuestions() ? mainClientId + "_" + questionKey : mainClientId;
        Object currentValue = isMultipleQuestions() ? getCurrentAnswer(questionKey) : getCurrentSingleValue();
        List<?> optionsList = resolveOptionsList(model != null ? model.getOptions() : getOptions());

        if (type == QuestionType.DESCRIPTIVE) {
            encodeDescriptive(context, writer, mainClientId, inputName, elementId + "_input", currentValue, disabled, readonly, required);
        } else {
            encodeChoice(context, writer, mainClientId, inputName, elementId, optionsList, currentValue, disabled, readonly, required);
        }

        writer.endElement("div"); // .box-question-body
    }

    private void encodeHeader(ResponseWriter writer, String prompt, Object number,
                             String description, boolean required) throws IOException {
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
                                   String mainClientId, String inputName, String inputId,
                                   Object currentValue, boolean disabled, boolean readonly,
                                   boolean required) throws IOException {
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-question-descriptive", null);

        writer.startElement("textarea", this);
        writer.writeAttribute("id", inputId, "id");
        writer.writeAttribute("name", inputName, null);
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
        if (required) {
            writer.writeAttribute("aria-required", "true", null);
        }

        String behaviorScript = buildBehaviorScript(context, mainClientId, "valueChange", "change");
        if (behaviorScript != null && !disabled && !readonly) {
            writer.writeAttribute("onchange", behaviorScript, null);
        }

        if (currentValue != null) {
            writer.writeText(String.valueOf(currentValue), "value");
        }

        writer.endElement("textarea");
        writer.endElement("div");
    }

    private void encodeChoice(FacesContext context, ResponseWriter writer,
                             String mainClientId, String inputName, String elementId,
                             List<?> optionsList, Object currentValue,
                             boolean disabled, boolean readonly, boolean required) throws IOException {
        writer.startElement("div", this);
        writer.writeAttribute("class", "box-question-options", null);

        String currentStr = currentValue != null ? String.valueOf(currentValue) : null;
        String behaviorScript = buildBehaviorScript(context, mainClientId, "valueChange", "change", "click");

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
            String optionId = elementId + "_opt" + index;

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
            writer.writeAttribute("name", inputName, null);
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
            if (required) {
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
