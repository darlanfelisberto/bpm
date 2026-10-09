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
    private OffsetDateTime inicioAplicacao;

    @Column(name = "fim_aplicacao", nullable = false)
    private OffsetDateTime fimAplicacao;

    @Column(nullable = false)
    private boolean anonimo = false;

    @Column(name = "permite_edicao", nullable = false)
    private boolean permiteEdicao = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusInstrumento status = StatusInstrumento.RASCUNHO;

    @Column(name = "consolidado_em")
    private OffsetDateTime consolidadoEm;

    @Column(name = "criado_por", nullable = false, length = 60)
    private String criadoPor;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @OneToMany(mappedBy = "instrumentoAvaliativo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PublicoAlvo> publicosAlvo = new ArrayList<>();

    @OneToMany(mappedBy = "instrumentoAvaliativo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<InstrumentoSessao> sessoes = new ArrayList<>();

    @OneToMany(mappedBy = "instrumentoAvaliativo")
    private List<Participacao> participacoes = new ArrayList<>();

    @OneToMany(mappedBy = "instrumentoAvaliativo")
    private List<RespostaInstrumento> respostas = new ArrayList<>();

    @OneToMany(mappedBy = "instrumentoAvaliativo")
    private List<Encaminhamento> encaminhamentos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        OffsetDateTime agora = OffsetDateTime.now();
        if (criadoEm == null) {
            criadoEm = agora;
        }
        if (atualizadoEm == null) {
            atualizadoEm = agora;
        }
    }

    @PreUpdate
    public void preUpdate() {
        atualizadoEm = OffsetDateTime.now();
    }

    public boolean isAberto() {
        OffsetDateTime agora = OffsetDateTime.now();
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNome() {
        return getTitulo();
    }

    public void setNome(String nome) {
        setTitulo(nome);
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

    public OffsetDateTime getInicioAplicacao() {
        return inicioAplicacao;
    }

    public void setInicioAplicacao(OffsetDateTime inicioAplicacao) {
        this.inicioAplicacao = inicioAplicacao;
    }

    public OffsetDateTime getFimAplicacao() {
        return fimAplicacao;
    }

    public void setFimAplicacao(OffsetDateTime fimAplicacao) {
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

    public StatusInstrumento getStatus() {
        return status;
    }

    public void setStatus(StatusInstrumento status) {
        this.status = status;
    }

    public OffsetDateTime getConsolidadoEm() {
        return consolidadoEm;
    }

    public void setConsolidadoEm(OffsetDateTime consolidadoEm) {
        this.consolidadoEm = consolidadoEm;
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

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(OffsetDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public List<PublicoAlvo> getPublicosAlvo() {
        return publicosAlvo;
    }

    public void setPublicosAlvo(List<PublicoAlvo> publicosAlvo) {
        this.publicosAlvo = publicosAlvo;
    }

    public List<InstrumentoSessao> getSessoes() {
        return sessoes;
    }

    public List<InstrumentoSessao> getGrupos() {
        return sessoes;
    }

    public void setSessoes(List<InstrumentoSessao> sessoes) {
        this.sessoes = sessoes;
    }

    public java.time.LocalDateTime getDataInicio() {
        return inicioAplicacao != null ? inicioAplicacao.toLocalDateTime() : null;
    }

    public void setDataInicio(java.time.LocalDateTime dataInicio) {
        this.inicioAplicacao = dataInicio != null ? dataInicio.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime() : null;
    }

    public java.time.LocalDateTime getDataFim() {
        return fimAplicacao != null ? fimAplicacao.toLocalDateTime() : null;
    }

    public void setDataFim(java.time.LocalDateTime dataFim) {
        this.fimAplicacao = dataFim != null ? dataFim.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime() : null;
    }

    public String getPublicoAlvoTipo() {
        return publicosAlvo.isEmpty() ? null : publicosAlvo.get(0).getEscopo().name();
    }

    public void setPublicoAlvoTipo(String publicoAlvoTipo) {
        if (publicoAlvoTipo != null && !publicoAlvoTipo.isBlank()) {
            PublicoAlvo pa = publicosAlvo.isEmpty() ? new PublicoAlvo() : publicosAlvo.get(0);
            pa.setInstrumentoAvaliativo(this);
            pa.setEscopo(EscopoPublicoAlvo.valueOf(publicoAlvoTipo));
            if (publicosAlvo.isEmpty()) {
                publicosAlvo.add(pa);
            }
        }
    }

    public String getPublicoAlvoDescricao() {
        return publicosAlvo.isEmpty() ? null : publicosAlvo.get(0).getReferenciaExterna();
    }

    public void setPublicoAlvoDescricao(String publicoAlvoDescricao) {
        if (!publicosAlvo.isEmpty()) {
            publicosAlvo.get(0).setReferenciaExterna(publicoAlvoDescricao);
        }
    }

    public List<Participacao> getParticipacoes() {
        return participacoes;
    }

    public void setParticipacoes(List<Participacao> participacoes) {
        this.participacoes = participacoes;
    }

    public List<RespostaInstrumento> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<RespostaInstrumento> respostas) {
        this.respostas = respostas;
    }

    public List<Encaminhamento> getEncaminhamentos() {
        return encaminhamentos;
    }

    public void setEncaminhamentos(List<Encaminhamento> encaminhamentos) {
        this.encaminhamentos = encaminhamentos;
    }

    @Override
    public UUID getMMId() {
        return this.instrumentoAvaliativoId;
    }
}
