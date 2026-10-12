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
import br.com.feliva.sharedClass.db.Model;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questao", schema = "avaliacao")
public class Questao extends Model<UUID> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "questao_id", updatable = false, nullable = false)
    private UUID id;

    @Override
    public UUID getMMId() {
        return this.id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conjunto_opcao_id")
    private ConjuntoOpcao conjuntoOpcao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_conhecimento_id")
    private AreaConhecimento areaConhecimento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoQuestao tipo;

    @Column(length = 100)
    private String assunto;

    @Column
    private Integer ano;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NivelQuestao nivel;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DificuldadeQuestao dificuldade;

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

    public AreaConhecimento getAreaConhecimento() {
        return areaConhecimento;
    }

    public void setAreaConhecimento(AreaConhecimento areaConhecimento) {
        this.areaConhecimento = areaConhecimento;
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

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public NivelQuestao getNivel() {
        return nivel;
    }

    public void setNivel(NivelQuestao nivel) {
        this.nivel = nivel;
    }

    public DificuldadeQuestao getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(DificuldadeQuestao dificuldade) {
        this.dificuldade = dificuldade;
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
