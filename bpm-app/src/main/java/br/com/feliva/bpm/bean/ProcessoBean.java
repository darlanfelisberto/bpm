package br.com.feliva.bpm.bean;

import br.com.feliva.bpm.model.Macroprocesso;
import br.com.feliva.bpm.model.Processo;
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
public class ProcessoBean implements Serializable {

    @Inject
    private EntityManager em;

    private Long macroprocessoId;
    private Processo novo = new Processo();

    public Long getMacroprocessoId() {
        return macroprocessoId;
    }

    public void setMacroprocessoId(Long macroprocessoId) {
        this.macroprocessoId = macroprocessoId;
    }

    public Macroprocesso getMacroprocesso() {
        return em.find(Macroprocesso.class, macroprocessoId);
    }

    public List<Processo> getLista() {
        return em.createQuery(
                        "select p from Processo p where p.macroprocesso.id = :mid order by p.nome",
                        Processo.class)
                .setParameter("mid", macroprocessoId)
                .getResultList();
    }

    public Processo getNovo() {
        return novo;
    }

    public String salvar() {
        novo.setMacroprocesso(getMacroprocesso());
        em.persist(novo);
        novo = new Processo();
        return null;
    }

    public void excluir(Processo processo) {
        em.remove(em.merge(processo));
    }
}
