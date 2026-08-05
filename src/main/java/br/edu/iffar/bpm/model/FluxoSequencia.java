package br.edu.iffar.bpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

/**
 * Aresta origem -> destino entre atividades. Modelada à parte (em vez de uma
 * coluna "proxima_atividade_id" na Atividade) para suportar bifurcações em
 * gateways (múltiplas saídas com rótulo de condição, ex: "Sim" / "Não").
 */
@Entity
@Table(name = "fluxo_sequencia")
public class FluxoSequencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "atividade_origem_id", nullable = false)
    private Atividade atividadeOrigem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "atividade_destino_id", nullable = false)
    private Atividade atividadeDestino;

    @Column(name = "rotulo_condicao", length = 60)
    private String rotuloCondicao;

    @Column(nullable = false)
    private Integer ordem = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Processo getProcesso() {
        return processo;
    }

    public void setProcesso(Processo processo) {
        this.processo = processo;
    }

    public Atividade getAtividadeOrigem() {
        return atividadeOrigem;
    }

    public void setAtividadeOrigem(Atividade atividadeOrigem) {
        this.atividadeOrigem = atividadeOrigem;
    }

    public Atividade getAtividadeDestino() {
        return atividadeDestino;
    }

    public void setAtividadeDestino(Atividade atividadeDestino) {
        this.atividadeDestino = atividadeDestino;
    }

    public String getRotuloCondicao() {
        return rotuloCondicao;
    }

    public void setRotuloCondicao(String rotuloCondicao) {
        this.rotuloCondicao = rotuloCondicao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
