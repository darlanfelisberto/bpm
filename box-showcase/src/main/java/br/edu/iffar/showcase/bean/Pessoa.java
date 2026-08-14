package br.edu.iffar.showcase.bean;

import java.io.Serializable;
import java.time.LocalDate;

/** Linha do dataset sintético usado pra demonstrar o b:datatable. */
public class Pessoa implements Serializable {

    private Long id;
    private String nome;
    private String email;
    private String cargo;
    private LocalDate dataAdmissao;

    public Pessoa() {
    }

    public Pessoa(Long id, String nome, String email, String cargo, LocalDate dataAdmissao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cargo = cargo;
        this.dataAdmissao = dataAdmissao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    @Override
    public String toString() {
        return nome + " (" + cargo + ")";
    }
}
