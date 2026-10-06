package br.edu.iffar.bpm.avaliacao.bean;

import br.edu.iffar.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.edu.iffar.bpm.avaliacao.model.ModuloAvaliacao;
import br.edu.iffar.bpm.avaliacao.model.StatusInstrumento;
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
@Transactional
public class InstrumentoBean implements Serializable {

    @Inject
    private EntityManager em;

    private InstrumentoAvaliativo novo = new InstrumentoAvaliativo();

    public List<InstrumentoAvaliativo> getLista() {
        return em.createQuery(
                        "select i from InstrumentoAvaliativo i order by i.inicioAplicacao desc",
                        InstrumentoAvaliativo.class)
                .getResultList();
    }

    public InstrumentoAvaliativo getNovo() {
        return novo;
    }

    public String salvar() {
        if (novo.getModulo() == null) {
            novo.setModulo(ModuloAvaliacao.AUTOAVALIACAO);
        }
        if (novo.getCicloReferencia() == null || novo.getCicloReferencia().isBlank()) {
            novo.setCicloReferencia(String.valueOf(Year.now().getValue()));
        }
        if (novo.getCriadoPor() == null || novo.getCriadoPor().isBlank()) {
            novo.setCriadoPor("admin");
        }
        if (novo.getStatus() == null) {
            novo.setStatus(StatusInstrumento.RASCUNHO);
        }
        em.persist(novo);
        novo = new InstrumentoAvaliativo();
        return null;
    }

    public void excluir(InstrumentoAvaliativo instrumento) {
        em.remove(em.merge(instrumento));
    }
}
