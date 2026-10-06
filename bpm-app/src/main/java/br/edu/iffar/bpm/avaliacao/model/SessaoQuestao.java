package br.edu.iffar.bpm.avaliacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessao_questao", schema = "avaliacao")
public class SessaoQuestao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sessao_questao_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_sessao_id", nullable = false)
    private InstrumentoSessao instrumentoSessao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @Column(nullable = false)
    private short ordem;

    @Column(nullable = false)
    private boolean obrigatoria = false;

    @Column(precision = 6, scale = 2)
    private BigDecimal peso;

    @OneToMany(mappedBy = "sessaoQuestao")
    private List<RespostaQuestao> respostas = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InstrumentoSessao getInstrumentoSessao() {
        return instrumentoSessao;
    }

    public void setInstrumentoSessao(InstrumentoSessao instrumentoSessao) {
        this.instrumentoSessao = instrumentoSessao;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    public short getOrdem() {
        return ordem;
    }

    public void setOrdem(short ordem) {
        this.ordem = ordem;
    }

    public boolean isObrigatoria() {
        return obrigatoria;
    }

    public void setObrigatoria(boolean obrigatoria) {
        this.obrigatoria = obrigatoria;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public List<RespostaQuestao> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<RespostaQuestao> respostas) {
        this.respostas = respostas;
    }

    public String getEnunciado() {
        return questao != null ? questao.getEnunciado() : null;
    }

    public TipoQuestao getTipo() {
        return questao != null ? questao.getTipo() : null;
    }

    public List<OpcaoQuestao> getOpcoes() {
        return questao != null && questao.getConjuntoOpcao() != null
                ? questao.getConjuntoOpcao().getOpcoes()
                : java.util.Collections.emptyList();
    }
}
