package br.edu.iffar.bpm.avaliacao.bean;

import br.edu.iffar.bpm.avaliacao.model.InstrumentoAvaliativo;
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
public class InstrumentoBean implements Serializable {

    @Inject
    private EntityManager em;

    private InstrumentoAvaliativo novo = new InstrumentoAvaliativo();

    public List<InstrumentoAvaliativo> getLista() {
        return em.createQuery(
                        "select i from InstrumentoAvaliativo i order by i.dataInicio desc",
                        InstrumentoAvaliativo.class)
                .getResultList();
    }

    public InstrumentoAvaliativo getNovo() {
        return novo;
    }

    public String salvar() {
        em.persist(novo);
        novo = new InstrumentoAvaliativo();
        return null;
    }

    public void excluir(InstrumentoAvaliativo instrumento) {
        em.remove(em.merge(instrumento));
    }
}
