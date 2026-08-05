package br.edu.iffar.bpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

/**
 * Tabela de referência (populada pela migration V1): START_EVENT, END_EVENT,
 * TASK, EXCLUSIVE_GATEWAY, PARALLEL_GATEWAY. Não é gerenciada via CRUD da aplicação.
 */
@Entity
@Table(name = "tipo_elemento")
public class TipoElemento implements Serializable {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "nome_exibicao", nullable = false, length = 60)
    private String nomeExibicao;

    @Column(name = "tag_bpmn", nullable = false, length = 60)
    private String tagBpmn;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public void setNomeExibicao(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getTagBpmn() {
        return tagBpmn;
    }

    public void setTagBpmn(String tagBpmn) {
        this.tagBpmn = tagBpmn;
    }
}
