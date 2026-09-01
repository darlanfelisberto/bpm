package br.edu.iffar.box.component.question;

/**
 * Represents an alternative option for single-choice questions in {@link Question}.
 * Applications with JPA entities (e.g. OpcaoQuestao) can implement this interface
 * or use {@link SimpleQuestionOption}.
 */
public interface QuestionOption {

    /**
     * Value or identifier submitted when this option is selected.
     */
    Object getValue();

    /**
     * Text label displayed next to the radio button.
     */
    String getLabel();

    /**
     * Optional secondary text or description for the option.
     */
    default String getDescription() {
        return null;
    }

    /**
     * Whether this specific option is disabled.
     */
    default boolean isDisabled() {
        return false;
    }
}
