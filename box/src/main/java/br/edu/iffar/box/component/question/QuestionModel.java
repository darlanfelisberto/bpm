package br.edu.iffar.box.component.question;

import java.util.List;

/**
 * Data model for questions rendered by {@link Question}.
 * Applications with database entities (e.g. Questao) can implement this
 * interface directly or use {@link SimpleQuestionModel}.
 */
public interface QuestionModel {

    /**
     * Unique identifier for this question.
     */
    Object getId();

    /**
     * The question statement or prompt text.
     */
    String getPrompt();

    /**
     * The format of the question (descriptive or choice).
     */
    QuestionType getType();

    /**
     * Whether an answer is required.
     */
    default boolean isRequired() {
        return false;
    }

    /**
     * Optional help text or supplementary instructions.
     */
    default String getDescription() {
        return null;
    }

    /**
     * Available options when type is {@link QuestionType#CHOICE}.
     */
    default List<? extends QuestionOption> getOptions() {
        return List.of();
    }
}
