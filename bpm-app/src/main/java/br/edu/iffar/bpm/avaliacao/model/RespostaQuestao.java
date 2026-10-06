package br.edu.iffar.bpm.avaliacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "resposta_questao", schema = "avaliacao")
public class RespostaQuestao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resposta_questao_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resposta_instrumento_id", nullable = false)
    private RespostaInstrumento respostaInstrumento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_questao_id", nullable = false)
    private SessaoQuestao sessaoQuestao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcao_questao_id")
    private OpcaoQuestao opcaoQuestao;

    @Column(columnDefinition = "TEXT")
    private String texto;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RespostaInstrumento getRespostaInstrumento() {
        return respostaInstrumento;
    }

    public void setRespostaInstrumento(RespostaInstrumento respostaInstrumento) {
        this.respostaInstrumento = respostaInstrumento;
    }

    public SessaoQuestao getSessaoQuestao() {
        return sessaoQuestao;
    }

    public void setSessaoQuestao(SessaoQuestao sessaoQuestao) {
        this.sessaoQuestao = sessaoQuestao;
    }

    public OpcaoQuestao getOpcaoQuestao() {
        return opcaoQuestao;
    }

    public void setOpcaoQuestao(OpcaoQuestao opcaoQuestao) {
        this.opcaoQuestao = opcaoQuestao;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
