package br.com.feliva.bpm.avaliacao.dao;

import br.com.feliva.bpm.avaliacao.model.InstrumentoSessao;
import br.com.feliva.sharedClass.db.InjectEntityManagerDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@RequestScoped
public class InstrumentoSessaoDAO extends InjectEntityManagerDAO<InstrumentoSessao> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(InstrumentoSessaoDAO.class.getName());

    public InstrumentoSessao buscarPorId(UUID id) {
        return em.find(InstrumentoSessao.class, id);
    }

    @Transactional
    public InstrumentoSessao salvar(InstrumentoSessao sessao) {
        if (sessao.getId() == null) {
            em.persist(sessao);
            em.flush();
            return sessao;
        } else {
            InstrumentoSessao mesclada = em.merge(sessao);
            em.flush();
            return mesclada;
        }
    }

    @Transactional
    public void excluir(InstrumentoSessao sessao) {
        if (sessao == null) {
            return;
        }
        InstrumentoSessao managed = em.contains(sessao) ? sessao : em.find(InstrumentoSessao.class, sessao.getId());
        if (managed != null) {
            if (managed.getInstrumentoAvaliativo() != null && managed.getInstrumentoAvaliativo().getSessoes() != null) {
                managed.getInstrumentoAvaliativo().getSessoes().remove(managed);
            }
            em.remove(managed);
            em.flush();
        }
    }

    @Transactional
    public void reordenar(List<InstrumentoSessao> sessoes, InstrumentoSessao sessao, int delta) {
        if (sessoes == null || sessao == null) {
            LOGGER.warning("reordenar: lista de sessoes ou sessao nula");
            return;
        }
        int idx = -1;
        for (int i = 0; i < sessoes.size(); i++) {
            if (sessoes.get(i).getId() != null && sessoes.get(i).getId().equals(sessao.getId())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            idx = sessoes.indexOf(sessao);
        }
        int targetIdx = idx + delta;
        LOGGER.info("reordenar sessao: id=" + sessao.getId() + " idx=" + idx + " targetIdx=" + targetIdx + " total=" + sessoes.size());
        if (idx >= 0 && targetIdx >= 0 && targetIdx < sessoes.size()) {
            java.util.Collections.swap(sessoes, idx, targetIdx);
            for (short i = 0; i < sessoes.size(); i++) {
                InstrumentoSessao item = sessoes.get(i);
                item.setOrdem((short) (i + 1));
                InstrumentoSessao managed = em.find(InstrumentoSessao.class, item.getId());
                if (managed != null) {
                    managed.setOrdem((short) (i + 1));
                } else {
                    em.merge(item);
                }
            }
            em.flush();
        }
    }
}
