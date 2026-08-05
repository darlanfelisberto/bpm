package br.edu.iffar.bpm.bean;

import br.edu.iffar.bpm.bpmn.BpmnXmlBuilder;
import br.edu.iffar.bpm.model.Atividade;
import br.edu.iffar.bpm.model.FluxoSequencia;
import br.edu.iffar.bpm.model.Processo;
import br.edu.iffar.bpm.model.Raia;
import br.edu.iffar.bpm.model.TipoElemento;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * Painel integrado de modelagem: grid de etapas (atividade + raia + tipo) e
 * a lista de fluxos de sequência do processo, na mesma tela.
 */
@Named
@ViewScoped
@Transactional
public class ProcessoDetalheBean implements Serializable {

    @Inject
    private EntityManager em;

    private Long processoId;

    private Atividade novaAtividade = new Atividade();
    private Long novaAtividadeRaiaId;
    private Integer novaAtividadeTipoElementoId;

    private FluxoSequencia novoFluxo = new FluxoSequencia();
    private Long novoFluxoOrigemId;
    private Long novoFluxoDestinoId;

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public Processo getProcesso() {
        return em.find(Processo.class, processoId);
    }

    public List<Raia> getRaias() {
        return em.createQuery(
                        "select r from Raia r where r.processo.id = :pid order by r.ordem", Raia.class)
                .setParameter("pid", processoId)
                .getResultList();
    }

    public List<TipoElemento> getTiposElemento() {
        return em.createQuery("select t from TipoElemento t order by t.id", TipoElemento.class)
                .getResultList();
    }

    public List<Atividade> getAtividades() {
        return em.createQuery(
                        "select a from Atividade a where a.processo.id = :pid order by a.ordem",
                        Atividade.class)
                .setParameter("pid", processoId)
                .getResultList();
    }

    public List<FluxoSequencia> getFluxos() {
        return em.createQuery(
                        "select f from FluxoSequencia f where f.processo.id = :pid order by f.ordem",
                        FluxoSequencia.class)
                .setParameter("pid", processoId)
                .getResultList();
    }

    public String getXmlBpmn() {
        return new BpmnXmlBuilder().gerar(getProcesso(), getRaias(), getAtividades(), getFluxos());
    }

    public Atividade getNovaAtividade() {
        return novaAtividade;
    }

    public Long getNovaAtividadeRaiaId() {
        return novaAtividadeRaiaId;
    }

    public void setNovaAtividadeRaiaId(Long novaAtividadeRaiaId) {
        this.novaAtividadeRaiaId = novaAtividadeRaiaId;
    }

    public Integer getNovaAtividadeTipoElementoId() {
        return novaAtividadeTipoElementoId;
    }

    public void setNovaAtividadeTipoElementoId(Integer novaAtividadeTipoElementoId) {
        this.novaAtividadeTipoElementoId = novaAtividadeTipoElementoId;
    }

    public FluxoSequencia getNovoFluxo() {
        return novoFluxo;
    }

    public Long getNovoFluxoOrigemId() {
        return novoFluxoOrigemId;
    }

    public void setNovoFluxoOrigemId(Long novoFluxoOrigemId) {
        this.novoFluxoOrigemId = novoFluxoOrigemId;
    }

    public Long getNovoFluxoDestinoId() {
        return novoFluxoDestinoId;
    }

    public void setNovoFluxoDestinoId(Long novoFluxoDestinoId) {
        this.novoFluxoDestinoId = novoFluxoDestinoId;
    }

    public String salvarAtividade() {
        novaAtividade.setProcesso(getProcesso());
        if (novaAtividadeRaiaId != null) {
            novaAtividade.setRaia(em.find(Raia.class, novaAtividadeRaiaId));
        }
        novaAtividade.setTipoElemento(em.find(TipoElemento.class, novaAtividadeTipoElementoId));
        em.persist(novaAtividade);
        novaAtividade = new Atividade();
        novaAtividadeRaiaId = null;
        novaAtividadeTipoElementoId = null;
        return null;
    }

    public void excluirAtividade(Atividade atividade) {
        em.remove(em.merge(atividade));
    }

    public String salvarFluxo() {
        novoFluxo.setProcesso(getProcesso());
        novoFluxo.setAtividadeOrigem(em.find(Atividade.class, novoFluxoOrigemId));
        novoFluxo.setAtividadeDestino(em.find(Atividade.class, novoFluxoDestinoId));
        em.persist(novoFluxo);
        novoFluxo = new FluxoSequencia();
        novoFluxoOrigemId = null;
        novoFluxoDestinoId = null;
        return null;
    }

    public void excluirFluxo(FluxoSequencia fluxo) {
        em.remove(em.merge(fluxo));
    }
}
