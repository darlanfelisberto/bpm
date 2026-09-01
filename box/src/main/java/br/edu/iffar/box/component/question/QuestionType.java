package br.edu.iffar.box.component.question;

/**
 * Supported question types for {@link Question}.
 */
public enum QuestionType {

    /** Open-ended question rendered as a multi-line textarea. */
    DESCRIPTIVE,

    /** Single-choice question rendered as a list of radio buttons. */
    CHOICE;

    /**
     * Parses a value into a {@link QuestionType}, case-insensitively,
     * defaulting to {@link #DESCRIPTIVE} if null or unrecognized.
     */
    public static QuestionType parse(Object value) {
        if (value instanceof QuestionType type) {
            return type;
        }
        if (value == null) {
            return DESCRIPTIVE;
        }
        String str = value.toString().trim().toUpperCase();
        try {
            return QuestionType.valueOf(str);
        } catch (IllegalArgumentException e) {
            return DESCRIPTIVE;
        }
    }
}
