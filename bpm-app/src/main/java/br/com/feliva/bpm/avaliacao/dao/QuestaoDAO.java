package br.com.feliva.bpm.avaliacao.dao;

import br.com.feliva.bpm.avaliacao.model.ConjuntoOpcao;
import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.com.feliva.bpm.avaliacao.model.InstrumentoSessao;
import br.com.feliva.bpm.avaliacao.model.Questao;
import br.com.feliva.bpm.avaliacao.model.SessaoQuestao;
import br.com.feliva.bpm.avaliacao.model.TipoQuestao;
import br.com.feliva.sharedClass.db.InjectEntityManagerDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class QuestaoDAO extends InjectEntityManagerDAO<Questao> {

    public Questao buscarPorId(UUID id) {
        return em.find(Questao.class, id);
    }

    public SessaoQuestao buscarSessaoQuestaoPorId(UUID id) {
        return em.find(SessaoQuestao.class, id);
    }

    @Transactional
    public SessaoQuestao salvarQuestao(InstrumentoSessao sessao, SessaoQuestao sessaoQuestao, Questao questao) {
        if (questao.getId() == null) {
            questao.setCriadoPor("admin");
            questao.setCriadoEm(OffsetDateTime.now());

            if (questao.getTipo() != TipoQuestao.DESCRITIVA && questao.getConjuntoOpcao() == null) {
                ConjuntoOpcao co = new ConjuntoOpcao();
                String prefixo = questao.getEnunciado() != null && questao.getEnunciado().length() > 30
                        ? questao.getEnunciado().substring(0, 30)
                        : (questao.getEnunciado() != null ? questao.getEnunciado() : "Opções");
                co.setNome(prefixo.trim() + " (" + UUID.randomUUID().toString().substring(0, 8) + ")");
                em.persist(co);
                questao.setConjuntoOpcao(co);
            }
            em.persist(questao);

            sessaoQuestao.setQuestao(questao);
            sessaoQuestao.setInstrumentoSessao(sessao);
            em.persist(sessaoQuestao);
            em.flush();
            return sessaoQuestao;
        } else {
            if (questao.getTipo() != TipoQuestao.DESCRITIVA && questao.getConjuntoOpcao() == null) {
                ConjuntoOpcao co = new ConjuntoOpcao();
                co.setNome("Opções (" + UUID.randomUUID().toString().substring(0, 8) + ")");
                em.persist(co);
                questao.setConjuntoOpcao(co);
            } else if (questao.getTipo() == TipoQuestao.DESCRITIVA) {
                questao.setConjuntoOpcao(null);
            }
            Questao questaoMesclada = em.merge(questao);
            sessaoQuestao.setQuestao(questaoMesclada);
            SessaoQuestao sessaoQuestaoMesclada = em.merge(sessaoQuestao);
            em.flush();
            return sessaoQuestaoMesclada;
        }
    }

    @Transactional
    public void excluirSessaoQuestao(SessaoQuestao sq, InstrumentoAvaliativo instrumento) {
        if (sq == null) {
            return;
        }
        SessaoQuestao managed = em.contains(sq) ? sq : em.find(SessaoQuestao.class, sq.getId());
        if (managed != null) {
            if (managed.getInstrumentoSessao() != null && managed.getInstrumentoSessao().getQuestoes() != null) {
                managed.getInstrumentoSessao().getQuestoes().remove(managed);
            }
            if (instrumento != null && instrumento.getSessoes() != null) {
                for (InstrumentoSessao s : instrumento.getSessoes()) {
                    if (s.getQuestoes() != null) {
                        s.getQuestoes().removeIf(q -> q.getId() != null && q.getId().equals(sq.getId()));
                    }
                }
            }
            em.remove(managed);
            em.flush();
        }
    }

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(QuestaoDAO.class.getName());

    @Transactional
    public void reordenar(List<SessaoQuestao> questoes, SessaoQuestao sq, int delta) {
        if (questoes == null || sq == null) {
            LOGGER.warning("reordenar questao: lista ou sq nulos");
            return;
        }
        int idx = -1;
        for (int i = 0; i < questoes.size(); i++) {
            if (questoes.get(i).getId() != null && questoes.get(i).getId().equals(sq.getId())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            idx = questoes.indexOf(sq);
        }
        int targetIdx = idx + delta;
        LOGGER.info("reordenar questao: id=" + sq.getId() + " idx=" + idx + " targetIdx=" + targetIdx + " total=" + questoes.size());
        if (idx >= 0 && targetIdx >= 0 && targetIdx < questoes.size()) {
            java.util.Collections.swap(questoes, idx, targetIdx);
            for (short i = 0; i < questoes.size(); i++) {
                SessaoQuestao item = questoes.get(i);
                item.setOrdem((short) (i + 1));
                SessaoQuestao managed = em.find(SessaoQuestao.class, item.getId());
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
