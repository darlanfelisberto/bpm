package br.com.feliva.bpm.avaliacao.model;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conjunto_opcao", schema = "avaliacao")
public class ConjuntoOpcao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conjunto_opcao_id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @OneToMany(mappedBy = "conjuntoOpcao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<OpcaoQuestao> opcoes = new ArrayList<>();

    @OneToMany(mappedBy = "conjuntoOpcao")
    private List<Questao> questoes = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<OpcaoQuestao> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(List<OpcaoQuestao> opcoes) {
        this.opcoes = opcoes;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }
}
