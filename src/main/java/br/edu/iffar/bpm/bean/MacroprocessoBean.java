package br.edu.iffar.bpm.bean;

import br.edu.iffar.bpm.model.Macroprocesso;
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
public class MacroprocessoBean implements Serializable {

    @Inject
    private EntityManager em;

    private Macroprocesso novo = new Macroprocesso();

    public List<Macroprocesso> getLista() {
        return em.createQuery("select m from Macroprocesso m order by m.nome", Macroprocesso.class)
                .getResultList();
    }

    public Macroprocesso getNovo() {
        return novo;
    }

    public String salvar() {
        em.persist(novo);
        novo = new Macroprocesso();
        return null;
    }

    public void excluir(Macroprocesso macroprocesso) {
        em.remove(em.merge(macroprocesso));
    }
}
