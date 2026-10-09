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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "encaminhamento", schema = "avaliacao")
public class Encaminhamento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "encaminhamento_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_avaliativo_id", nullable = false)
    private InstrumentoAvaliativo instrumentoAvaliativo;

    @Column(name = "alvo_ref", length = 60)
    private String alvoRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 22)
    private TipoEncaminhamento tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "responsavel_ref", nullable = false, length = 60)
    private String responsavelRef;

    @Column
    private LocalDate prazo;

    @Column(name = "registrado_em", nullable = false)
    private OffsetDateTime registradoEm;

    @PrePersist
    public void prePersist() {
        if (registradoEm == null) {
            registradoEm = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InstrumentoAvaliativo getInstrumentoAvaliativo() {
        return instrumentoAvaliativo;
    }

    public void setInstrumentoAvaliativo(InstrumentoAvaliativo instrumentoAvaliativo) {
        this.instrumentoAvaliativo = instrumentoAvaliativo;
    }

    public String getAlvoRef() {
        return alvoRef;
    }

    public void setAlvoRef(String alvoRef) {
        this.alvoRef = alvoRef;
    }

    public TipoEncaminhamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEncaminhamento tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getResponsavelRef() {
        return responsavelRef;
    }

    public void setResponsavelRef(String responsavelRef) {
        this.responsavelRef = responsavelRef;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    public OffsetDateTime getRegistradoEm() {
        return registradoEm;
    }

    public void setRegistradoEm(OffsetDateTime registradoEm) {
        this.registradoEm = registradoEm;
    }
}
