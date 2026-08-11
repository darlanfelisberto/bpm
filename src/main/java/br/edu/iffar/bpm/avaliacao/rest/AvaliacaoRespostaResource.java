package br.edu.iffar.bpm.avaliacao.rest;

import br.edu.iffar.bpm.avaliacao.model.GrupoQuestao;
import br.edu.iffar.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.edu.iffar.bpm.avaliacao.model.OpcaoQuestao;
import br.edu.iffar.bpm.avaliacao.model.Questao;
import br.edu.iffar.bpm.avaliacao.model.RespostaInstrumento;
import br.edu.iffar.bpm.avaliacao.model.RespostaQuestao;
import br.edu.iffar.bpm.avaliacao.rest.dto.ErroValidacaoDTO;
import br.edu.iffar.bpm.avaliacao.rest.dto.GrupoDTO;
import br.edu.iffar.bpm.avaliacao.rest.dto.InstrumentoRespostaDTO;
import br.edu.iffar.bpm.avaliacao.rest.dto.OpcaoDTO;
import br.edu.iffar.bpm.avaliacao.rest.dto.QuestaoDTO;
import br.edu.iffar.bpm.avaliacao.rest.dto.RespostaEnvioDTO;
import br.edu.iffar.bpm.avaliacao.rest.dto.RespostaItemDTO;
import br.edu.iffar.bpm.avaliacao.rest.dto.RespostaStatusDTO;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * API consumida pela SPA de resposta (frontend/). O "token" identifica o
 * preenchimento no navegador (ver frontend/src/token.ts): quando o
 * instrumento é anônimo ele não tem nenhuma ligação com a pessoa, só serve
 * para retomar um rascunho (RF06/RF07/RF08).
 */
@Path("/avaliacao/instrumentos/{instrumentoId}")
public class AvaliacaoRespostaResource {

    @Inject
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response obterInstrumento(@PathParam("instrumentoId") Long instrumentoId) {
        InstrumentoAvaliativo instrumento = em.find(InstrumentoAvaliativo.class, instrumentoId);
        if (instrumento == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(paraDTO(instrumento)).build();
    }

    @GET
    @Path("/respostas/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response obterResposta(@PathParam("instrumentoId") Long instrumentoId, @PathParam("token") String token) {
        RespostaInstrumento resposta = buscarResposta(instrumentoId, token);
        if (resposta == null) {
            return Response.noContent().build();
        }
        return Response.ok(paraStatusDTO(resposta)).build();
    }

    @PUT
    @Path("/respostas/{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response salvarResposta(@PathParam("instrumentoId") Long instrumentoId, @PathParam("token") String token,
                                    RespostaEnvioDTO envio) {
        InstrumentoAvaliativo instrumento = em.find(InstrumentoAvaliativo.class, instrumentoId);
        if (instrumento == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (!instrumento.isAberto()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErroValidacaoDTO(List.of("O período de aplicação deste instrumento está fechado.")))
                    .build();
        }

        RespostaInstrumento resposta = buscarResposta(instrumentoId, token);
        if (resposta == null) {
            resposta = new RespostaInstrumento();
            resposta.setInstrumento(instrumento);
            resposta.setRespondenteRef(token);
            resposta.setStatus("PARCIAL");
            em.persist(resposta);
        }

        List<String> erros = new ArrayList<>();
        for (GrupoQuestao grupo : instrumento.getGrupos()) {
            for (Questao questao : grupo.getQuestoes()) {
                RespostaItemDTO item = buscarItem(envio, questao.getId());
                boolean respondida = item != null
                        && (item.opcaoId() != null || (item.textoLivre() != null && !item.textoLivre().isBlank()));

                if (envio.completo() && questao.isObrigatoria() && !respondida) {
                    erros.add("Pergunta obrigatória não respondida: " + questao.getEnunciado());
                    continue;
                }
                if (!respondida) {
                    continue;
                }

                RespostaQuestao rq = buscarOuCriarRespostaQuestao(resposta, questao);
                if (item.opcaoId() != null) {
                    rq.setOpcao(em.find(OpcaoQuestao.class, item.opcaoId()));
                    rq.setTextoLivre(null);
                } else {
                    rq.setOpcao(null);
                    rq.setTextoLivre(item.textoLivre());
                }
            }
        }

        if (envio.completo() && !erros.isEmpty()) {
            return Response.status(422).entity(new ErroValidacaoDTO(erros)).build();
        }

        if (envio.completo()) {
            resposta.setStatus("COMPLETO");
            resposta.setEnviadoEm(LocalDateTime.now());
        } else {
            resposta.setStatus("PARCIAL");
        }

        return Response.ok(paraStatusDTO(resposta)).build();
    }

    private RespostaInstrumento buscarResposta(Long instrumentoId, String token) {
        List<RespostaInstrumento> existentes = em.createQuery(
                        "select r from RespostaInstrumento r where r.instrumento.id = :iid and r.respondenteRef = :ref",
                        RespostaInstrumento.class)
                .setParameter("iid", instrumentoId)
                .setParameter("ref", token)
                .getResultList();
        return existentes.isEmpty() ? null : existentes.get(0);
    }

    private RespostaQuestao buscarOuCriarRespostaQuestao(RespostaInstrumento resposta, Questao questao) {
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

    private static RespostaItemDTO buscarItem(RespostaEnvioDTO envio, Long questaoId) {
        return envio.respostas().stream()
                .filter(item -> item.questaoId().equals(questaoId))
                .findFirst()
                .orElse(null);
    }

    private RespostaStatusDTO paraStatusDTO(RespostaInstrumento resposta) {
        List<RespostaQuestao> itens = em.createQuery(
                        "select rq from RespostaQuestao rq where rq.respostaInstrumento.id = :rid",
                        RespostaQuestao.class)
                .setParameter("rid", resposta.getId())
                .getResultList();
        List<RespostaItemDTO> respostas = itens.stream()
                .map(rq -> new RespostaItemDTO(
                        rq.getQuestao().getId(),
                        rq.getOpcao() != null ? rq.getOpcao().getId() : null,
                        rq.getTextoLivre()))
                .toList();
        return new RespostaStatusDTO(resposta.getStatus(), respostas);
    }

    private static InstrumentoRespostaDTO paraDTO(InstrumentoAvaliativo instrumento) {
        List<GrupoDTO> grupos = instrumento.getGrupos().stream()
                .map(AvaliacaoRespostaResource::paraDTO)
                .toList();
        return new InstrumentoRespostaDTO(
                instrumento.getId(),
                instrumento.getTitulo(),
                instrumento.getDescricao(),
                instrumento.isAnonimo(),
                instrumento.isAberto(),
                grupos);
    }

    private static GrupoDTO paraDTO(GrupoQuestao grupo) {
        List<QuestaoDTO> questoes = grupo.getQuestoes().stream()
                .map(AvaliacaoRespostaResource::paraDTO)
                .toList();
        return new GrupoDTO(grupo.getId(), grupo.getTitulo(), grupo.getDescricao(), questoes);
    }

    private static QuestaoDTO paraDTO(Questao questao) {
        List<OpcaoDTO> opcoes = questao.getOpcoes().stream()
                .map(o -> new OpcaoDTO(o.getId(), o.getTexto()))
                .toList();
        return new QuestaoDTO(questao.getId(), questao.getEnunciado(), questao.getTipo().name(),
                questao.isObrigatoria(), opcoes);
    }
}
