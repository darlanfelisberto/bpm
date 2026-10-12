package br.com.feliva.bpm.avaliacao.model;

public enum DificuldadeQuestao {
    MUITO_FACIL("Muito Fácil"),
    FACIL("Fácil"),
    MEDIA("Média"),
    DIFICIL("Difícil"),
    MUITO_DIFICIL("Muito Difícil");

    private final String descricao;

    DificuldadeQuestao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
