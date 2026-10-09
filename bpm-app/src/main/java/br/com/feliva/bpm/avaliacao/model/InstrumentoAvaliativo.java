package br.com.feliva.bpm.avaliacao.model;

import br.com.feliva.sharedClass.db.Model;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instrumento_avaliativo", schema = "avaliacao")
public class InstrumentoAvaliativo extends Model<UUID> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "instrumento_avaliativo_id", updatable = false, nullable = false)
    private UUID instrumentoAvaliativoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_avaliativo_origem_id")
    private InstrumentoAvaliativo instrumentoAvaliativoOrigem;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ciclo_referencia", nullable = false, length = 20)
    private String cicloReferencia;

    @Column(name = "inicio_aplicacao", nullable = false)
    private LocalDateTime inicioAplicacao;

    @Column(name = "fim_aplicacao", nullable = false)
    private LocalDateTime fimAplicacao;

    @Column(nullable = false)
    private boolean anonimo = false;

    @Column(name = "permite_edicao", nullable = false)
    private boolean permiteEdicao = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_status_id")
    private TipoStatus tipoStatus;

    @Column(name = "consolidado_em")
    private LocalDateTime consolidadoEm;

    @Column(name = "criado_por", nullable = false, length = 60)
    private String criadoPor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

//    @OneToMany(mappedBy = "instrumentoAvaliativo", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<PublicoAlvo> publicosAlvo = new ArrayList<>();
//
//    @OneToMany(mappedBy = "instrumentoAvaliativo", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OrderBy("ordem ASC")
//    private List<InstrumentoSessao> sessoes = new ArrayList<>();
//
//    @OneToMany(mappedBy = "instrumentoAvaliativo")
//    private List<Participacao> participacoes = new ArrayList<>();
//
//    @OneToMany(mappedBy = "instrumentoAvaliativo")
//    private List<RespostaInstrumento> respostas = new ArrayList<>();
//
//    @OneToMany(mappedBy = "instrumentoAvaliativo")
//    private List<Encaminhamento> encaminhamentos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        if (criadoEm == null) {
            criadoEm = agora;
        }
        if (atualizadoEm == null) {
            atualizadoEm = agora;
        }
    }

    @PreUpdate
    public void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public boolean isAberto() {
        LocalDateTime agora = LocalDateTime.now();
        return inicioAplicacao != null && fimAplicacao != null
                && !agora.isBefore(inicioAplicacao) && !agora.isAfter(fimAplicacao);
    }

    public UUID getId() {
        return instrumentoAvaliativoId;
    }

    public void setId(UUID id) {
        this.instrumentoAvaliativoId = id;
    }

    public InstrumentoAvaliativo getInstrumentoAvaliativoOrigem() {
        return instrumentoAvaliativoOrigem;
    }

    public void setInstrumentoAvaliativoOrigem(InstrumentoAvaliativo instrumentoAvaliativoOrigem) {
        this.instrumentoAvaliativoOrigem = instrumentoAvaliativoOrigem;
    }



//    public void setPublicoAlvoTipo(String publicoAlvoTipo) {
//        if (publicoAlvoTipo != null && !publicoAlvoTipo.isBlank()) {
//            PublicoAlvo pa = publicosAlvo.isEmpty() ? new PublicoAlvo() : publicosAlvo.get(0);
//            pa.setInstrumentoAvaliativo(this);
//            pa.setEscopo(EscopoPublicoAlvo.valueOf(publicoAlvoTipo));
//            if (publicosAlvo.isEmpty()) {
//                publicosAlvo.add(pa);
//            }
//        }
//    }
//
//    public String getPublicoAlvoDescricao() {
//        return publicosAlvo.isEmpty() ? null : publicosAlvo.get(0).getReferenciaExterna();
//    }
//
//    public void setPublicoAlvoDescricao(String publicoAlvoDescricao) {
//        if (!publicosAlvo.isEmpty()) {
//            publicosAlvo.get(0).setReferenciaExterna(publicoAlvoDescricao);
//        }
//    }

    public UUID getInstrumentoAvaliativoId() {
        return instrumentoAvaliativoId;
    }

    public void setInstrumentoAvaliativoId(UUID instrumentoAvaliativoId) {
        this.instrumentoAvaliativoId = instrumentoAvaliativoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCicloReferencia() {
        return cicloReferencia;
    }

    public void setCicloReferencia(String cicloReferencia) {
        this.cicloReferencia = cicloReferencia;
    }

    public LocalDateTime getInicioAplicacao() {
        return inicioAplicacao;
    }

    public void setInicioAplicacao(LocalDateTime inicioAplicacao) {
        this.inicioAplicacao = inicioAplicacao;
    }

    public LocalDateTime getFimAplicacao() {
        return fimAplicacao;
    }

    public void setFimAplicacao(LocalDateTime fimAplicacao) {
        this.fimAplicacao = fimAplicacao;
    }

    public boolean isAnonimo() {
        return anonimo;
    }

    public void setAnonimo(boolean anonimo) {
        this.anonimo = anonimo;
    }

    public boolean isPermiteEdicao() {
        return permiteEdicao;
    }

    public void setPermiteEdicao(boolean permiteEdicao) {
        this.permiteEdicao = permiteEdicao;
    }

    public TipoStatus getTipoStatus() {
        return tipoStatus;
    }

    public void setTipoStatus(TipoStatus tipoStatus) {
        this.tipoStatus = tipoStatus;
    }

    public LocalDateTime getConsolidadoEm() {
        return consolidadoEm;
    }

    public void setConsolidadoEm(LocalDateTime consolidadoEm) {
        this.consolidadoEm = consolidadoEm;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    @Override
    public UUID getMMId() {
        return this.instrumentoAvaliativoId;
    }
}
