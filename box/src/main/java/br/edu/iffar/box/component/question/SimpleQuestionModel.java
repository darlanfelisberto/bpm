package br.edu.iffar.box.component.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard implementation of {@link QuestionModel}.
 */
public class SimpleQuestionModel implements QuestionModel, Serializable {

    private Object id;
    private String prompt;
    private QuestionType type = QuestionType.DESCRIPTIVE;
    private boolean required;
    private String description;
    private List<QuestionOption> options = new ArrayList<>();

    public SimpleQuestionModel() {
    }

    public SimpleQuestionModel(Object id, String prompt, QuestionType type) {
        this.id = id;
        this.prompt = prompt;
        this.type = type;
    }

    public SimpleQuestionModel(Object id, String prompt, QuestionType type, boolean required) {
        this.id = id;
        this.prompt = prompt;
        this.type = type;
        this.required = required;
    }

    @Override
    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }

    public SimpleQuestionModel addOption(Object value, String label) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(new SimpleQuestionOption(value, label));
        return this;
    }

    public SimpleQuestionModel addOption(Object value, String label, String description) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(new SimpleQuestionOption(value, label, description));
        return this;
    }
}
