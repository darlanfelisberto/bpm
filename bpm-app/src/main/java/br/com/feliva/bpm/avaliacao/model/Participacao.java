package br.com.feliva.bpm.avaliacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "participacao", schema = "avaliacao")
public class Participacao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "participacao_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_avaliativo_id", nullable = false)
    private InstrumentoAvaliativo instrumentoAvaliativo;

    @Column(name = "usuario_ref", nullable = false, length = 60)
    private String usuarioRef;

    @Column(name = "alvo_ref", length = 60)
    private String alvoRef;

    @Column(name = "registrada_em", nullable = false)
    private OffsetDateTime registradaEm;

    @PrePersist
    public void prePersist() {
        if (registradaEm == null) {
            registradaEm = OffsetDateTime.now();
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

    public String getUsuarioRef() {
        return usuarioRef;
    }

    public void setUsuarioRef(String usuarioRef) {
        this.usuarioRef = usuarioRef;
    }

    public String getAlvoRef() {
        return alvoRef;
    }

    public void setAlvoRef(String alvoRef) {
        this.alvoRef = alvoRef;
    }

    public OffsetDateTime getRegistradaEm() {
        return registradaEm;
    }

    public void setRegistradaEm(OffsetDateTime registradaEm) {
        this.registradaEm = registradaEm;
    }
}
