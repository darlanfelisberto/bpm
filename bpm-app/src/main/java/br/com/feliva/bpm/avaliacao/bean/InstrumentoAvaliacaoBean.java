package br.com.feliva.bpm.avaliacao.bean;

import br.com.feliva.bpm.avaliacao.dao.InstrumentoAvaliacaoDAO;
import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.com.feliva.bpm.avaliacao.model.StatusInstrumento;
import br.edu.iffar.box.component.datatable.DatatableLazyModel;
import br.edu.iffar.box.component.datatable.DatatablePage;
import br.edu.iffar.box.component.datatable.DatatableQuery;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.time.Year;
import java.util.List;

@Named
@ViewScoped
public class InstrumentoAvaliacaoBean implements Serializable, DatatableLazyModel<InstrumentoAvaliativo> {

    private String nome;
    private boolean buscaRealizada = false;
    private InstrumentoAvaliativo novo = new InstrumentoAvaliativo();

    @Inject
    private InstrumentoAvaliacaoDAO instrumentoAvaliacaoDAO;

    public void buscar() {
        this.buscaRealizada = true;
    }

    @Override
    @Transactional
    public DatatablePage<InstrumentoAvaliativo> load(DatatableQuery query) {
        if (!buscaRealizada) {
            return new DatatablePage<>(List.of(), 0);
        }
        return instrumentoAvaliacaoDAO.carregar(query, nome);
    }

    @Override
    public int defaultPageSize() {
        return 10;
    }

    public boolean isBuscaRealizada() {
        return buscaRealizada;
    }

    public void setBuscaRealizada(boolean buscaRealizada) {
        this.buscaRealizada = buscaRealizada;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

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

        try {
            instrumentoAvaliacaoDAO.saveOrUpdate(novo);
            novo = new InstrumentoAvaliativo();
        } catch (Exception e) {
            // Log/mensagem caso necessário
        }
        return null;
    }

    public void excluir(InstrumentoAvaliativo instrumento) {
        try {
            instrumentoAvaliacaoDAO.remove(instrumento);
        } catch (Exception e) {
            // Log/mensagem caso necessário
        }
    }
}
