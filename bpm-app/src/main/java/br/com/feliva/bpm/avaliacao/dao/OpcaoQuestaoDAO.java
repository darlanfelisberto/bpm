package br.com.feliva.bpm.avaliacao.dao;

import br.com.feliva.bpm.avaliacao.model.ConjuntoOpcao;
import br.com.feliva.bpm.avaliacao.model.OpcaoQuestao;
import br.com.feliva.bpm.avaliacao.model.Questao;
import br.com.feliva.sharedClass.db.InjectEntityManagerDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@RequestScoped
public class OpcaoQuestaoDAO extends InjectEntityManagerDAO<OpcaoQuestao> {

    public OpcaoQuestao buscarPorId(UUID id) {
        return em.find(OpcaoQuestao.class, id);
    }

    public ConjuntoOpcao buscarConjuntoPorId(UUID id) {
        return em.find(ConjuntoOpcao.class, id);
    }

    @Transactional
    public ConjuntoOpcao obterOuCriarConjunto(Questao questao) {
        ConjuntoOpcao conjunto = questao.getConjuntoOpcao();
        if (conjunto == null) {
            conjunto = new ConjuntoOpcao();
            conjunto.setNome("Opções (" + UUID.randomUUID().toString().substring(0, 8) + ")");
            em.persist(conjunto);
            questao.setConjuntoOpcao(conjunto);
            em.merge(questao);
            em.flush();
        }
        return conjunto;
    }

    @Transactional
    public OpcaoQuestao adicionarOpcao(Questao questao, String texto, BigDecimal valor) {
        ConjuntoOpcao conjunto = obterOuCriarConjunto(questao);

        OpcaoQuestao op = new OpcaoQuestao();
        op.setConjuntoOpcao(conjunto);
        op.setTexto(texto != null ? texto.trim() : "");
        op.setValor(valor);
        short proximaOrdem = (short) (conjunto.getOpcoes() != null ? conjunto.getOpcoes().size() + 1 : 1);
        op.setOrdem(proximaOrdem);
        em.persist(op);
        em.flush();
        return op;
    }

    @Transactional
    public void excluirOpcao(OpcaoQuestao opcao) {
        if (opcao == null) {
            return;
        }
        OpcaoQuestao managed = em.contains(opcao) ? opcao : em.find(OpcaoQuestao.class, opcao.getId());
        if (managed != null) {
            if (managed.getConjuntoOpcao() != null && managed.getConjuntoOpcao().getOpcoes() != null) {
                managed.getConjuntoOpcao().getOpcoes().remove(managed);
            }
            em.remove(managed);
            em.flush();
        }
    }

    @Transactional
    public void aplicarTemplate(Questao questao, String[] textos, BigDecimal[] valores) {
        if (questao == null || textos == null) {
            return;
        }
        ConjuntoOpcao conjunto = obterOuCriarConjunto(questao);

        for (int i = 0; i < textos.length; i++) {
            OpcaoQuestao op = new OpcaoQuestao();
            op.setConjuntoOpcao(conjunto);
            op.setTexto(textos[i]);
            op.setValor(valores != null && i < valores.length ? valores[i] : null);
            op.setOrdem((short) (i + 1));
            em.persist(op);
        }
        em.flush();
    }
}
