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
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instrumento_sessao", schema = "avaliacao")
public class InstrumentoSessao  extends Model<UUID> implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "instrumento_sessao_id", updatable = false, nullable = false)
    private UUID instumentoSessaoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_avaliativo_id", nullable = false)
    private InstrumentoAvaliativo instrumentoAvaliativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private TipoSessao tipo;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private short ordem;
    @OneToMany(mappedBy = "instrumentoSessao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<SessaoQuestao> questoes = new ArrayList<>();

    @Override
    public UUID getMMId() {
        return this.instumentoSessaoId;
    }

    public UUID getId() {
        return instumentoSessaoId;
    }

    public void setId(UUID id) {
        this.instumentoSessaoId = id;
    }

    public UUID getInstumentoSessaoId() {
        return instumentoSessaoId;
    }

    public void setInstumentoSessaoId(UUID instumentoSessaoId) {
        this.instumentoSessaoId = instumentoSessaoId;
    }

    public InstrumentoAvaliativo getInstrumentoAvaliativo() {
        return instrumentoAvaliativo;
    }

    public void setInstrumentoAvaliativo(InstrumentoAvaliativo instrumentoAvaliativo) {
        this.instrumentoAvaliativo = instrumentoAvaliativo;
    }

    public TipoSessao getTipo() {
        return tipo;
    }

    public void setTipo(TipoSessao tipo) {
        this.tipo = tipo;
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

    public short getOrdem() {
        return ordem;
    }

    public void setOrdem(short ordem) {
        this.ordem = ordem;
    }

    public List<SessaoQuestao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<SessaoQuestao> questoes) {
        this.questoes = questoes;
    }
}
