package br.edu.iffar.bpm.bean;

import br.edu.iffar.bpm.model.Processo;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;

/**
 * Só expõe os dados do processo para o título da página; a leitura/escrita
 * do diagrama em si acontece via ProcessoDiagramaResource (REST), chamado
 * direto pelo bpmn-js Modeler no navegador.
 */
@Named
@ViewScoped
@Transactional
public class ProcessoDetalheBean implements Serializable {

    @Inject
    private EntityManager em;

    private Long processoId;

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public Processo getProcesso() {
        return em.find(Processo.class, processoId);
    }
}
