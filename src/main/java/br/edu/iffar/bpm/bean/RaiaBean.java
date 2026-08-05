package br.edu.iffar.bpm.bean;

import br.edu.iffar.bpm.model.Processo;
import br.edu.iffar.bpm.model.Raia;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@Transactional
public class RaiaBean implements Serializable {

    @Inject
    private EntityManager em;

    private Long processoId;
    private Raia novo = new Raia();

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public Processo getProcesso() {
        return em.find(Processo.class, processoId);
    }

    public List<Raia> getLista() {
        return em.createQuery(
                        "select r from Raia r where r.processo.id = :pid order by r.ordem", Raia.class)
                .setParameter("pid", processoId)
                .getResultList();
    }

    public Raia getNovo() {
        return novo;
    }

    public void editar(Raia raia) {
        this.novo = raia;
    }

    public void cancelarEdicao() {
        this.novo = new Raia();
    }

    public String salvar() {
        if (novo.getId() == null) {
            novo.setProcesso(getProcesso());
            em.persist(novo);
        } else {
            em.merge(novo);
        }
        novo = new Raia();
        return null;
    }

    public void excluir(Raia raia) {
        em.remove(em.merge(raia));
    }
}
