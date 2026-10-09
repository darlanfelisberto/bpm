package br.com.feliva.bpm.avaliacao.dao;

import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
//import br.com.feliva.bpm.avaliacao.model.StatusInstrumento;
import br.com.feliva.bpm.avaliacao.model.TipoStatus;
import br.com.feliva.sharedClass.db.InjectEntityManagerDAO;
import br.edu.iffar.box.component.datatable.DatatablePage;
import br.edu.iffar.box.component.datatable.DatatableQuery;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestScoped
public class InstrumentoAvaliacaoDAO extends InjectEntityManagerDAO<InstrumentoAvaliativo> {

    public InstrumentoAvaliativo buscarPorId(UUID id) {
        return em.find(InstrumentoAvaliativo.class, id);
    }

    public List<TipoStatus> getTiposStatus() {
        return em.createQuery("select ts from TipoStatus ts order by ts.tipoStatusId", TipoStatus.class)
                .getResultList();
    }

    public TipoStatus buscarTipoStatusPorId(String id) {
        return em.find(TipoStatus.class, id);
    }

    public List<InstrumentoAvaliativo> getLista() {
        return em.createQuery(
                        "select i from InstrumentoAvaliativo i order by i.inicioAplicacao desc",
                        InstrumentoAvaliativo.class)
                .getResultList();
    }

    public DatatablePage<InstrumentoAvaliativo> carregar(DatatableQuery query, String filtroNome) {
        StringBuilder where = new StringBuilder(" where 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (filtroNome != null && !filtroNome.isBlank()) {
            where.append(" and lower(i.titulo) like :filtroNomePrincipal ");
            params.put("filtroNomePrincipal", "%" + filtroNome.trim().toLowerCase() + "%");
        }

        if (query != null && query.filters() != null) {
            for (Map.Entry<String, String> entry : query.filters().entrySet()) {
                String field = entry.getKey();
                String val = entry.getValue();
                if (val != null && !val.isBlank()) {
                    String paramKey = "filter_" + field.replace(".", "_");
                    if ("titulo".equalsIgnoreCase(field) || "nome".equalsIgnoreCase(field)) {
                        where.append(" and lower(i.titulo) like :").append(paramKey);
                        params.put(paramKey, "%" + val.trim().toLowerCase() + "%");
                    } else if ("cicloReferencia".equalsIgnoreCase(field)) {
                        where.append(" and lower(i.cicloReferencia) like :").append(paramKey);
                        params.put(paramKey, "%" + val.trim().toLowerCase() + "%");
                    } else if ("tipoStatus".equalsIgnoreCase(field)) {
                        try {
//                            StatusInstrumento st = StatusInstrumento.valueOf(val.trim().toUpperCase());
//                            where.append(" and i.status = :").append(paramKey);
//                            params.put(paramKey, st);
                        } catch (IllegalArgumentException e) {
                            where.append(" and lower(cast(i.tipoStatus as string)) like :").append(paramKey);
                            params.put(paramKey, "%" + val.trim().toLowerCase() + "%");
                        }
                    } else if ("descricao".equalsIgnoreCase(field)) {
                        where.append(" and lower(i.descricao) like :").append(paramKey);
                        params.put(paramKey, "%" + val.trim().toLowerCase() + "%");
                    }
                }
            }
        }

        String countJpql = "select count(i) from InstrumentoAvaliativo i " + where;
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        params.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        if (total == 0 || (query != null && query.first() >= total)) {
            return new DatatablePage<>(List.of(), total);
        }

        StringBuilder order = new StringBuilder(" order by ");
        String sortBy = query != null ? query.sortBy() : null;
        if (sortBy != null && !sortBy.isBlank()) {
            String prop = switch (sortBy) {
                case "nome", "titulo" -> "i.titulo";
                case "cicloReferencia" -> "i.cicloReferencia";
                case "tipoStatus" -> "i.tipoStatus";
                case "inicioAplicacao" -> "i.inicioAplicacao";
                case "fimAplicacao" -> "i.fimAplicacao";
                case "criadoEm" -> "i.criadoEm";
                default -> "i.titulo";
            };
            order.append(prop).append(query.sortAscending() ? " asc" : " desc");
        } else {
            order.append("i.inicioAplicacao desc, i.titulo asc");
        }

        String dataJpql = "select i from InstrumentoAvaliativo i " + where + order;
        TypedQuery<InstrumentoAvaliativo> dataQuery = em.createQuery(dataJpql, InstrumentoAvaliativo.class);
        params.forEach(dataQuery::setParameter);

        if (query != null) {
            dataQuery.setFirstResult(query.first());
            dataQuery.setMaxResults(query.size());
        }

        List<InstrumentoAvaliativo> resultados = dataQuery.getResultList();
        return new DatatablePage<>(resultados, total);
    }
}
