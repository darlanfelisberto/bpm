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
import java.time.LocalDateTime;

@Entity
@Table(name = "resposta_instrumento", schema = "avaliacao")
public class RespostaInstrumento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private InstrumentoAvaliativo instrumento;

    @Column(name = "respondente_ref", length = 200)
    private String respondenteRef;

    @Column(nullable = false, length = 20)
    private String status = "PARCIAL";

    @Column(name = "iniciado_em", insertable = false, updatable = false)
    private LocalDateTime iniciadoEm;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InstrumentoAvaliativo getInstrumento() {
        return instrumento;
    }

    public void setInstrumento(InstrumentoAvaliativo instrumento) {
        this.instrumento = instrumento;
    }

    public String getRespondenteRef() {
        return respondenteRef;
    }

    public void setRespondenteRef(String respondenteRef) {
        this.respondenteRef = respondenteRef;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }
}
