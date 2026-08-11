package br.edu.iffar.bpm.avaliacao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "instrumento_avaliativo", schema = "avaliacao")
public class InstrumentoAvaliativo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private boolean anonimo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Column(name = "publico_alvo_tipo", nullable = false, length = 20)
    private String publicoAlvoTipo;

    @Column(name = "publico_alvo_descricao", length = 200)
    private String publicoAlvoDescricao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "instrumento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<GrupoQuestao> grupos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isAnonimo() {
        return anonimo;
    }

    public void setAnonimo(boolean anonimo) {
        this.anonimo = anonimo;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public String getPublicoAlvoTipo() {
        return publicoAlvoTipo;
    }

    public void setPublicoAlvoTipo(String publicoAlvoTipo) {
        this.publicoAlvoTipo = publicoAlvoTipo;
    }

    public String getPublicoAlvoDescricao() {
        return publicoAlvoDescricao;
    }

    public void setPublicoAlvoDescricao(String publicoAlvoDescricao) {
        this.publicoAlvoDescricao = publicoAlvoDescricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public List<GrupoQuestao> getGrupos() {
        return grupos;
    }

    public boolean isAberto() {
        LocalDateTime agora = LocalDateTime.now();
        return !agora.isBefore(dataInicio) && !agora.isAfter(dataFim);
    }
}
