package br.com.feliva.bpm.avaliacao.bean;

import br.com.feliva.bpm.avaliacao.dao.InstrumentoAvaliacaoDAO;
import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.com.feliva.bpm.avaliacao.model.TipoStatus;
import br.edu.iffar.box.component.datatable.DatatableLazyModel;
import br.edu.iffar.box.component.datatable.DatatablePage;
import br.edu.iffar.box.component.datatable.DatatableQuery;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
@Transactional
public class InstrumentoAvaliacaoBean implements Serializable, DatatableLazyModel<InstrumentoAvaliativo> {

    private String nome;
    private boolean buscaRealizada = false;
    private boolean editando = false;
    private InstrumentoAvaliativo instrumento = new InstrumentoAvaliativo();
    private String tipoStatusId;

    @Inject
    private InstrumentoAvaliacaoDAO instrumentoAvaliacaoDAO;

    public void buscar() {
        this.buscaRealizada = true;
    }

    @Override
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

    public void novo() {
        this.instrumento = new InstrumentoAvaliativo();
        this.tipoStatusId = "RASCUNHO";
        this.editando = true;
    }

    public void editar() {
        FacesContext context = FacesContext.getCurrentInstance();
        String idParam = context.getExternalContext().getRequestParameterMap().get("instrumentoId");
        if (idParam != null && !idParam.isBlank()) {
            InstrumentoAvaliativo carregado = instrumentoAvaliacaoDAO.buscarPorId(UUID.fromString(idParam));
            if (carregado != null) {
                editar(carregado);
                return;
            }
        }
        this.instrumento = new InstrumentoAvaliativo();
        this.tipoStatusId = "RASCUNHO";
        this.editando = true;
    }

    public void editar(InstrumentoAvaliativo inst) {
        if (inst == null) {
            editar();
            return;
        }
        if (inst.getId() != null) {
            InstrumentoAvaliativo carregado = instrumentoAvaliacaoDAO.buscarPorId(inst.getId());
            this.instrumento = carregado != null ? carregado : inst;
        } else {
            this.instrumento = inst;
        }
        this.tipoStatusId = this.instrumento != null && this.instrumento.getTipoStatus() != null
                ? this.instrumento.getTipoStatus().getTipoStatusId()
                : "RASCUNHO";
        this.editando = true;
    }

    public void cancelar() {
        this.editando = false;
        this.instrumento = new InstrumentoAvaliativo();
        this.tipoStatusId = null;
    }

    @Transactional
    public String salvar() {
        if (instrumento.getInicioAplicacao() != null && instrumento.getFimAplicacao() != null
                && !instrumento.getFimAplicacao().isAfter(instrumento.getInicioAplicacao())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "A data de término deve ser posterior à data de início.", null));
            return null;
        }

        try {
            if (tipoStatusId != null && !tipoStatusId.isBlank()) {
                instrumento.setTipoStatus(instrumentoAvaliacaoDAO.buscarTipoStatusPorId(tipoStatusId));
            } else if (instrumento.getTipoStatus() == null) {
                instrumento.setTipoStatus(instrumentoAvaliacaoDAO.buscarTipoStatusPorId("RASCUNHO"));
            }

            if ("CONSOLIDADO".equals(tipoStatusId)) {
                if (instrumento.getConsolidadoEm() == null) {
                    instrumento.setConsolidadoEm(LocalDateTime.now());
                }
            } else {
                instrumento.setConsolidadoEm(null);
            }

            if (instrumento.getCriadoPor() == null || instrumento.getCriadoPor().isBlank()) {
                instrumento.setCriadoPor("admin");
            }
            if (instrumento.getCicloReferencia() == null || instrumento.getCicloReferencia().isBlank()) {
                instrumento.setCicloReferencia(String.valueOf(Year.now().getValue()));
            }

            instrumentoAvaliacaoDAO.saveOrUpdate(instrumento);
            this.editando = false;
            this.instrumento = new InstrumentoAvaliativo();
            this.tipoStatusId = null;
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar instrumento: " + e.getMessage(), null));
        }
        return null;
    }

    @Transactional
    public void excluir() {
        FacesContext context = FacesContext.getCurrentInstance();
        String idParam = context.getExternalContext().getRequestParameterMap().get("instrumentoId");
        if (idParam != null && !idParam.isBlank()) {
            excluir(UUID.fromString(idParam));
        }
    }

    @Transactional
    public void excluir(UUID id) {
        if (id != null) {
            InstrumentoAvaliativo inst = instrumentoAvaliacaoDAO.buscarPorId(id);
            if (inst != null) {
                excluir(inst);
            }
        }
    }

    @Transactional
    public void excluir(InstrumentoAvaliativo instrumento) {
        if (instrumento == null) {
            excluir();
            return;
        }
        try {
            instrumentoAvaliacaoDAO.remove(instrumento);
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir instrumento: " + e.getMessage(), null));
        }
    }

    public List<TipoStatus> getTiposStatus() {
        return instrumentoAvaliacaoDAO.getTiposStatus();
    }

    public boolean isBuscaRealizada() {
        return buscaRealizada;
    }

    public void setBuscaRealizada(boolean buscaRealizada) {
        this.buscaRealizada = buscaRealizada;
    }

    public boolean isEditando() {
        return editando;
    }

    public void setEditando(boolean editando) {
        this.editando = editando;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public InstrumentoAvaliativo getInstrumento() {
        return instrumento;
    }

    public void setInstrumento(InstrumentoAvaliativo instrumento) {
        this.instrumento = instrumento;
    }

    public InstrumentoAvaliativo getNovo() {
        return instrumento;
    }

    public void setNovo(InstrumentoAvaliativo novo) {
        this.instrumento = novo;
    }

    public String getTipoStatusId() {
        return tipoStatusId;
    }

    public void setTipoStatusId(String tipoStatusId) {
        this.tipoStatusId = tipoStatusId;
    }
}
