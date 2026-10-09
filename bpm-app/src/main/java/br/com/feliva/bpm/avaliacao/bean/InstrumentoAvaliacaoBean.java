package br.com.feliva.bpm.avaliacao.bean;

import br.com.feliva.bpm.avaliacao.dao.InstrumentoAvaliacaoDAO;
import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.com.feliva.bpm.avaliacao.model.ModuloAvaliacao;
import br.com.feliva.bpm.avaliacao.model.StatusInstrumento;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.time.Year;
import java.util.List;

@Named
@ViewScoped
public class InstrumentoAvaliacaoBean implements Serializable {

    private String nome;

    private InstrumentoAvaliativo novo = new InstrumentoAvaliativo();

    @Inject
    private InstrumentoAvaliacaoDAO instrumentoAvaliacaiDAO;


    public InstrumentoAvaliativo getNovo() {
        return novo;
    }

    public String salvar() {

        if (novo.getCicloReferencia() == null || novo.getCicloReferencia().isBlank()) {
            novo.setCicloReferencia(String.valueOf(Year.now().getValue()));
        }
        if (novo.getCriadoPor() == null || novo.getCriadoPor().isBlank()) {
            novo.setCriadoPor("admin");
        }
        if (novo.getStatus() == null) {
            novo.setStatus(StatusInstrumento.RASCUNHO);
        }

        novo = new InstrumentoAvaliativo();
        return null;
    }

    public void excluir(InstrumentoAvaliativo instrumento) {

    }

    public void buscar(){
        System.out.println("buscar");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
