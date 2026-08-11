package br.edu.iffar.bpm.avaliacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "resposta_questao", schema = "avaliacao")
public class RespostaQuestao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "resposta_instrumento_id", nullable = false)
    private RespostaInstrumento respostaInstrumento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @ManyToOne
    @JoinColumn(name = "opcao_id")
    private OpcaoQuestao opcao;

    @Column(name = "texto_livre", columnDefinition = "TEXT")
    private String textoLivre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RespostaInstrumento getRespostaInstrumento() {
        return respostaInstrumento;
    }

    public void setRespostaInstrumento(RespostaInstrumento respostaInstrumento) {
        this.respostaInstrumento = respostaInstrumento;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    public OpcaoQuestao getOpcao() {
        return opcao;
    }

    public void setOpcao(OpcaoQuestao opcao) {
        this.opcao = opcao;
    }

    public String getTextoLivre() {
        return textoLivre;
    }

    public void setTextoLivre(String textoLivre) {
        this.textoLivre = textoLivre;
    }
}
