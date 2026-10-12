package br.com.feliva.bpm.avaliacao.dao;

import br.com.feliva.bpm.avaliacao.model.InstrumentoSessao;
import br.com.feliva.sharedClass.db.InjectEntityManagerDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@RequestScoped
public class InstrumentoSessaoDAO extends InjectEntityManagerDAO<InstrumentoSessao> {

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
            return;
        }
        int idx = sessoes.indexOf(sessao);
        int targetIdx = idx + delta;
        if (idx >= 0 && targetIdx >= 0 && targetIdx < sessoes.size()) {
            InstrumentoSessao vizinho = sessoes.get(targetIdx);
            short ordemOriginal = sessao.getOrdem();
            sessao.setOrdem(vizinho.getOrdem());
            vizinho.setOrdem(ordemOriginal);
            em.merge(sessao);
            em.merge(vizinho);
            em.flush();
        }
    }
}
