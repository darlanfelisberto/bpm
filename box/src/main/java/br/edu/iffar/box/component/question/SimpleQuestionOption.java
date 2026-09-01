package br.edu.iffar.box.component.question;

import java.io.Serializable;
import java.util.Objects;

/**
 * Standard implementation of {@link QuestionOption}.
 */
public class SimpleQuestionOption implements QuestionOption, Serializable {

    private Object value;
    private String label;
    private String description;
    private boolean disabled;

    public SimpleQuestionOption() {
    }

    public SimpleQuestionOption(Object value, String label) {
        this(value, label, null, false);
    }

    public SimpleQuestionOption(Object value, String label, String description) {
        this(value, label, description, false);
    }

    public SimpleQuestionOption(Object value, String label, String description, boolean disabled) {
        this.value = value;
        this.label = label;
        this.description = description;
        this.disabled = disabled;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionOption that)) return false;
        return Objects.equals(value, that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return label != null ? label : String.valueOf(value);
    }
}
