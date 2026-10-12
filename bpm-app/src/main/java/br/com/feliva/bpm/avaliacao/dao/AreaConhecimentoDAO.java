package br.com.feliva.bpm.avaliacao.dao;

import br.com.feliva.bpm.avaliacao.model.AreaConhecimento;
import br.com.feliva.sharedClass.db.InjectEntityManagerDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@RequestScoped
public class AreaConhecimentoDAO extends InjectEntityManagerDAO<AreaConhecimento> {

    public AreaConhecimento buscarPorId(UUID id) {
        return em.find(AreaConhecimento.class, id);
    }

    public List<AreaConhecimento> pesquisar(String termo, int maxResultados) {
        String jpql = "select a from AreaConhecimento a " +
                      "where lower(a.cod) like :termo or lower(a.descricao) like :termo " +
                      "order by a.cod asc";
        String pattern = "%" + (termo != null ? termo.trim().toLowerCase() : "") + "%";
        return em.createQuery(jpql, AreaConhecimento.class)
                .setParameter("termo", pattern)
                .setMaxResults(maxResultados > 0 ? maxResultados : 20)
                .getResultList();
    }

    public List<AreaConhecimento> listarTodas() {
        return em.createQuery("select a from AreaConhecimento a order by a.cod asc", AreaConhecimento.class)
                .getResultList();
    }

    @Transactional
    public AreaConhecimento salvar(AreaConhecimento area) {
        if (area.getId() == null) {
            em.persist(area);
            em.flush();
            return area;
        } else {
            AreaConhecimento mesclada = em.merge(area);
            em.flush();
            return mesclada;
        }
    }
}
