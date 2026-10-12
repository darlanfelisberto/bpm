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

    @Transactional
    public void reordenar(List<SessaoQuestao> questoes, SessaoQuestao sq, int delta) {
        if (questoes == null || sq == null) {
            return;
        }
        int idx = questoes.indexOf(sq);
        int targetIdx = idx + delta;
        if (idx >= 0 && targetIdx >= 0 && targetIdx < questoes.size()) {
            SessaoQuestao vizinho = questoes.get(targetIdx);
            short ordemOriginal = sq.getOrdem();
            sq.setOrdem(vizinho.getOrdem());
            vizinho.setOrdem(ordemOriginal);
            em.merge(sq);
            em.merge(vizinho);
            em.flush();
        }
    }
}
