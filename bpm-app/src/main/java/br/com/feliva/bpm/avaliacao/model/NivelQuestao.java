package br.com.feliva.bpm.avaliacao.model;

public enum NivelQuestao {
    FUNDAMENTAL("Ensino Fundamental"),
    MEDIO("Ensino Médio"),
    TECNICO("Ensino Técnico"),
    TECNOLOGO("Ensino Tecnológico"),
    GRADUACAO("Graduação"),
    POS_GRADUACAO("Pós-Graduação");

    private final String descricao;

    NivelQuestao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
