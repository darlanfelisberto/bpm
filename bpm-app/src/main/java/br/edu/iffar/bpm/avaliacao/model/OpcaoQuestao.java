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
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "opcao_questao", schema = "avaliacao")
public class OpcaoQuestao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "opcao_questao_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conjunto_opcao_id", nullable = false)
    private ConjuntoOpcao conjuntoOpcao;

    @Column(nullable = false, length = 300)
    private String texto;

    @Column(precision = 6, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private boolean correta = false;

    @Column(nullable = false)
    private short ordem;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ConjuntoOpcao getConjuntoOpcao() {
        return conjuntoOpcao;
    }

    public void setConjuntoOpcao(ConjuntoOpcao conjuntoOpcao) {
        this.conjuntoOpcao = conjuntoOpcao;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public boolean isCorreta() {
        return correta;
    }

    public void setCorreta(boolean correta) {
        this.correta = correta;
    }

    public short getOrdem() {
        return ordem;
    }

    public void setOrdem(short ordem) {
        this.ordem = ordem;
    }
}
