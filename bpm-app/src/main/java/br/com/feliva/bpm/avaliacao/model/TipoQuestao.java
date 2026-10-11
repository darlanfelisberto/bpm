package br.com.feliva.bpm.avaliacao.model;

public enum TipoQuestao {
    ESCOLHA_UNICA("Escolha Única (Apenas 1 opção)"),
    MULTIPLA_ESCOLHA("Múltipla Escolha (Várias opções)"),
    ESCALA("Escala de Avaliação / Likert"),
    DESCRITIVA("Texto Descritivo / Aberto");

    private final String descricao;

    TipoQuestao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
