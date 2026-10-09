package br.com.feliva.bpm.avaliacao.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resposta_instrumento", schema = "avaliacao")
public class RespostaInstrumento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resposta_instrumento_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_avaliativo_id", nullable = false)
    private InstrumentoAvaliativo instrumentoAvaliativo;

    @Column(name = "alvo_ref", length = 60)
    private String alvoRef;

    @Column(name = "usuario_ref", length = 60)
    private String usuarioRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusResposta status = StatusResposta.EM_ANDAMENTO;

    @Column(name = "data_envio")
    private LocalDate dataEnvio;

    @OneToMany(mappedBy = "respostaInstrumento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespostaQuestao> respostas = new ArrayList<>();

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

    public String getUsuarioRef() {
        return usuarioRef;
    }

    public void setUsuarioRef(String usuarioRef) {
        this.usuarioRef = usuarioRef;
    }

    public StatusResposta getStatus() {
        return status;
    }

    public void setStatus(StatusResposta status) {
        this.status = status;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDate dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public List<RespostaQuestao> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<RespostaQuestao> respostas) {
        this.respostas = respostas;
    }
}
