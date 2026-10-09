package br.com.feliva.bpm.avaliacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questao", schema = "avaliacao")
public class Questao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "questao_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conjunto_opcao_id")
    private ConjuntoOpcao conjuntoOpcao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoQuestao tipo;

    @Column(length = 100)
    private String assunto;

    @Column(name = "criado_por", nullable = false, length = 60)
    private String criadoPor;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @OneToMany(mappedBy = "questao")
    private List<SessaoQuestao> sessoesQuestao = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = OffsetDateTime.now();
        }
    }

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

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public TipoQuestao getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuestao tipo) {
        this.tipo = tipo;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<SessaoQuestao> getSessoesQuestao() {
        return sessoesQuestao;
    }

    public void setSessoesQuestao(List<SessaoQuestao> sessoesQuestao) {
        this.sessoesQuestao = sessoesQuestao;
    }
}
