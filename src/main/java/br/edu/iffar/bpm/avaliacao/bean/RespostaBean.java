package br.edu.iffar.bpm.avaliacao.bean;

import br.edu.iffar.bpm.avaliacao.model.GrupoQuestao;
import br.edu.iffar.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.edu.iffar.bpm.avaliacao.model.OpcaoQuestao;
import br.edu.iffar.bpm.avaliacao.model.Questao;
import br.edu.iffar.bpm.avaliacao.model.RespostaInstrumento;
import br.edu.iffar.bpm.avaliacao.model.RespostaQuestao;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Preenchimento de um instrumento. A "identidade" do respondente é apenas um
 * token guardado na URL/localStorage do navegador (ver responder.xhtml):
 * quando o instrumento é anônimo, esse token não é ligado a nenhuma pessoa,
 * só serve para retomar um preenchimento parcial (RF06/RF07).
 */
@Named
@ViewScoped
@Transactional
public class RespostaBean implements Serializable {

    @Inject
    private EntityManager em;

    private Long instrumentoId;
    private String respondenteRef;

    private RespostaInstrumento resposta;
    // Ligado direto ao h:selectOneRadio: o EL MapELResolver não coage o valor
    // submetido (String) para o tipo genérico do Map, então fica String aqui
    // e só é convertido para Long na hora de salvar.
    private final Map<Long, String> opcaoEscolhida = new HashMap<>();
    private final Map<Long, String> textoLivre = new HashMap<>();

    public Long getInstrumentoId() {
        return instrumentoId;
    }

    public void setInstrumentoId(Long instrumentoId) {
        this.instrumentoId = instrumentoId;
    }

    public String getRespondenteRef() {
        return respondenteRef;
    }

    public void setRespondenteRef(String respondenteRef) {
        this.respondenteRef = respondenteRef;
    }

    public InstrumentoAvaliativo getInstrumento() {
        return em.find(InstrumentoAvaliativo.class, instrumentoId);
    }

    public Map<Long, String> getOpcaoEscolhida() {
        return opcaoEscolhida;
    }

    public Map<Long, String> getTextoLivre() {
        return textoLivre;
    }

    public boolean isJaEnviada() {
        return resposta != null && "COMPLETO".equals(resposta.getStatus());
    }

    public void carregar() {
        if (resposta != null || instrumentoId == null || respondenteRef == null || respondenteRef.isBlank()) {
            return;
        }
        List<RespostaInstrumento> existentes = em.createQuery(
                        "select r from RespostaInstrumento r where r.instrumento.id = :iid and r.respondenteRef = :ref",
                        RespostaInstrumento.class)
                .setParameter("iid", instrumentoId)
                .setParameter("ref", respondenteRef)
                .getResultList();
        if (existentes.isEmpty()) {
            return;
        }
        resposta = existentes.get(0);
        List<RespostaQuestao> respostas = em.createQuery(
                        "select rq from RespostaQuestao rq where rq.respostaInstrumento.id = :rid",
                        RespostaQuestao.class)
                .setParameter("rid", resposta.getId())
                .getResultList();
        for (RespostaQuestao rq : respostas) {
            if (rq.getOpcao() != null) {
                opcaoEscolhida.put(rq.getQuestao().getId(), String.valueOf(rq.getOpcao().getId()));
            } else {
                textoLivre.put(rq.getQuestao().getId(), rq.getTextoLivre());
            }
        }
    }

    private void garantirResposta() {
        if (resposta == null) {
            resposta = new RespostaInstrumento();
            resposta.setInstrumento(getInstrumento());
            resposta.setRespondenteRef(respondenteRef);
            em.persist(resposta);
        }
    }

    private RespostaQuestao buscarOuCriar(Questao questao) {
        List<RespostaQuestao> existentes = em.createQuery(
                        "select rq from RespostaQuestao rq where rq.respostaInstrumento.id = :rid and rq.questao.id = :qid",
                        RespostaQuestao.class)
                .setParameter("rid", resposta.getId())
                .setParameter("qid", questao.getId())
                .getResultList();
        if (!existentes.isEmpty()) {
            return existentes.get(0);
        }
        RespostaQuestao rq = new RespostaQuestao();
        rq.setRespostaInstrumento(resposta);
        rq.setQuestao(questao);
        em.persist(rq);
        return rq;
    }

    public String salvarRascunho() {
        salvar(false);
        return null;
    }

    public String enviar() {
        salvar(true);
        return null;
    }

    private void salvar(boolean completo) {
        InstrumentoAvaliativo instrumento = getInstrumento();
        if (!instrumento.isAberto()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "O período de aplicação deste instrumento está fechado.", null));
            return;
        }

        garantirResposta();
        boolean faltaObrigatoria = false;
        for (GrupoQuestao grupo : instrumento.getGrupos()) {
            for (Questao questao : grupo.getQuestoes()) {
                String opcaoIdStr = opcaoEscolhida.get(questao.getId());
                String texto = textoLivre.get(questao.getId());
                boolean respondida = (opcaoIdStr != null && !opcaoIdStr.isBlank()) || (texto != null && !texto.isBlank());

                if (completo && questao.isObrigatoria() && !respondida) {
                    faltaObrigatoria = true;
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR, "Pergunta obrigatória não respondida: " + questao.getEnunciado(), null));
                    continue;
                }
                if (!respondida) {
                    continue;
                }

                RespostaQuestao rq = buscarOuCriar(questao);
                if (opcaoIdStr != null && !opcaoIdStr.isBlank()) {
                    rq.setOpcao(em.find(OpcaoQuestao.class, Long.valueOf(opcaoIdStr)));
                    rq.setTextoLivre(null);
                } else {
                    rq.setOpcao(null);
                    rq.setTextoLivre(texto);
                }
            }
        }

        if (completo && !faltaObrigatoria) {
            resposta.setStatus("COMPLETO");
            resposta.setEnviadoEm(LocalDateTime.now());
        } else if (!completo) {
            resposta.setStatus("PARCIAL");
        }
    }
}
