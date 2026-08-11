package br.edu.iffar.bpm.avaliacao.bean;

import br.edu.iffar.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.edu.iffar.bpm.avaliacao.model.Questao;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Relatório essencial (RF09): participação e, por questão, média (escala),
 * contagem por opção (múltipla escolha) ou respostas em texto livre.
 * Considera apenas respostas com status COMPLETO.
 */
@Named
@ViewScoped
@Transactional
public class RelatorioBean implements Serializable {

    @Inject
    private EntityManager em;

    private Long instrumentoId;

    public Long getInstrumentoId() {
        return instrumentoId;
    }

    public void setInstrumentoId(Long instrumentoId) {
        this.instrumentoId = instrumentoId;
    }

    public InstrumentoAvaliativo getInstrumento() {
        return em.find(InstrumentoAvaliativo.class, instrumentoId);
    }

    public long getTotalCompletas() {
        return contar("COMPLETO");
    }

    public long getTotalParciais() {
        return contar("PARCIAL");
    }

    private long contar(String status) {
        return em.createQuery(
                        "select count(r) from RespostaInstrumento r where r.instrumento.id = :iid and r.status = :status",
                        Long.class)
                .setParameter("iid", instrumentoId)
                .setParameter("status", status)
                .getSingleResult();
    }

    public Double mediaQuestao(Questao questao) {
        return em.createQuery(
                        "select avg(rq.opcao.valor) from RespostaQuestao rq "
                                + "where rq.questao.id = :qid and rq.opcao.valor is not null "
                                + "and rq.respostaInstrumento.status = 'COMPLETO'",
                        Double.class)
                .setParameter("qid", questao.getId())
                .getSingleResult();
    }

    public Map<String, Long> contagemOpcoes(Questao questao) {
        List<Object[]> linhas = em.createQuery(
                        "select rq.opcao.texto, count(rq) from RespostaQuestao rq "
                                + "where rq.questao.id = :qid and rq.opcao is not null "
                                + "and rq.respostaInstrumento.status = 'COMPLETO' "
                                + "group by rq.opcao.texto, rq.opcao.ordem order by rq.opcao.ordem",
                        Object[].class)
                .setParameter("qid", questao.getId())
                .getResultList();
        Map<String, Long> contagem = new LinkedHashMap<>();
        for (Object[] linha : linhas) {
            contagem.put((String) linha[0], (Long) linha[1]);
        }
        return contagem;
    }

    public List<String> respostasTexto(Questao questao) {
        return em.createQuery(
                        "select rq.textoLivre from RespostaQuestao rq "
                                + "where rq.questao.id = :qid and rq.textoLivre is not null "
                                + "and rq.respostaInstrumento.status = 'COMPLETO'",
                        String.class)
                .setParameter("qid", questao.getId())
                .getResultList();
    }
}
