package br.edu.iffar.box.component.question;

/**
 * Supported question types for {@link Question}.
 */
public enum QuestionType {

    /** Open-ended question rendered as a multi-line textarea. */
    DESCRIPTIVE,

    /** Single-choice question rendered as a list of radio buttons. */
    CHOICE,

    /** Multiple-choice question rendered as a list of checkboxes. */
    MULTIPLE_CHOICE,

    /** Rating or Likert scale question rendered as a horizontal pill/button row. */
    SCALE;

    /**
     * Parses a value into a {@link QuestionType}, case-insensitively,
     * supporting aliases like ESCOLHA_UNICA, MULTIPLA_ESCOLHA, ESCALA, DESCRITIVA,
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
        return switch (str) {
            case "CHOICE", "ESCOLHA_UNICA", "SINGLE_CHOICE", "RADIO" -> CHOICE;
            case "MULTIPLE_CHOICE", "MULTIPLA_ESCOLHA", "CHECKBOX" -> MULTIPLE_CHOICE;
            case "SCALE", "ESCALA", "LIKERT", "RATING" -> SCALE;
            case "DESCRIPTIVE", "DESCRITIVA", "TEXT", "TEXTAREA" -> DESCRIPTIVE;
            default -> {
                try {
                    yield QuestionType.valueOf(str);
                } catch (IllegalArgumentException e) {
                    yield DESCRIPTIVE;
                }
            }
        };
    }
}
