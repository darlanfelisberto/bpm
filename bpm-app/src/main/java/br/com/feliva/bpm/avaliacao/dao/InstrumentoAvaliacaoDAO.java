package br.com.feliva.bpm.avaliacao.dao;

import br.com.feliva.sharedClass.db.InjectEntityManagerDAO;
import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import jakarta.enterprise.context.RequestScoped;

import java.util.List;

@RequestScoped
public class InstrumentoAvaliacaoDAO extends InjectEntityManagerDAO<InstrumentoAvaliativo> {

    public List<InstrumentoAvaliativo> getLista() {
        return em.createQuery(
                        "select i from InstrumentoAvaliativo i order by i.inicioAplicacao desc",
                        InstrumentoAvaliativo.class)
                .getResultList();
    }
}
