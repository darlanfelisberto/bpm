package br.com.feliva.bpm.avaliacao.rest;

import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.com.feliva.bpm.avaliacao.model.InstrumentoSessao;
import br.com.feliva.bpm.avaliacao.model.OpcaoQuestao;
import br.com.feliva.bpm.avaliacao.model.Questao;
import br.com.feliva.bpm.avaliacao.model.RespostaInstrumento;
import br.com.feliva.bpm.avaliacao.model.RespostaQuestao;
import br.com.feliva.bpm.avaliacao.model.SessaoQuestao;
import br.com.feliva.bpm.avaliacao.model.StatusResposta;
import br.com.feliva.bpm.avaliacao.rest.dto.ErroValidacaoDTO;
import br.com.feliva.bpm.avaliacao.rest.dto.GrupoDTO;
import br.com.feliva.bpm.avaliacao.rest.dto.InstrumentoRespostaDTO;
import br.com.feliva.bpm.avaliacao.rest.dto.OpcaoDTO;
import br.com.feliva.bpm.avaliacao.rest.dto.QuestaoDTO;
import br.com.feliva.bpm.avaliacao.rest.dto.RespostaEnvioDTO;
import br.com.feliva.bpm.avaliacao.rest.dto.RespostaItemDTO;
import br.com.feliva.bpm.avaliacao.rest.dto.RespostaStatusDTO;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/avaliacao/instrumentos/{instrumentoId}")
public class AvaliacaoRespostaResource {

    @Inject
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response obterInstrumento(@PathParam("instrumentoId") UUID instrumentoId) {
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
    public Response obterResposta(@PathParam("instrumentoId") UUID instrumentoId, @PathParam("token") String token) {
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
    public Response salvarResposta(@PathParam("instrumentoId") UUID instrumentoId, @PathParam("token") String token,
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
            resposta.setInstrumentoAvaliativo(instrumento);
            resposta.setUsuarioRef(token);
            resposta.setStatus(StatusResposta.EM_ANDAMENTO);
            em.persist(resposta);
        }

        List<String> erros = new ArrayList<>();
        for (InstrumentoSessao sessao : instrumento.getSessoes()) {
            for (SessaoQuestao sq : sessao.getQuestoes()) {
                Questao questao = sq.getQuestao();
                RespostaItemDTO item = buscarItem(envio, questao.getId());
                boolean respondida = item != null
                        && (item.opcaoId() != null || (item.textoLivre() != null && !item.textoLivre().isBlank()));

                if (envio.completo() && sq.isObrigatoria() && !respondida) {
                    erros.add("Pergunta obrigatória não respondida: " + questao.getEnunciado());
                    continue;
                }
                if (!respondida) {
                    continue;
                }

                RespostaQuestao rq = buscarOuCriarRespostaQuestao(resposta, sq);
                if (item.opcaoId() != null) {
                    rq.setOpcaoQuestao(em.find(OpcaoQuestao.class, item.opcaoId()));
                    rq.setTexto(null);
                } else {
                    rq.setOpcaoQuestao(null);
                    rq.setTexto(item.textoLivre());
                }
            }
        }

        if (envio.completo() && !erros.isEmpty()) {
            return Response.status(422).entity(new ErroValidacaoDTO(erros)).build();
        }

        if (envio.completo()) {
            resposta.setStatus(StatusResposta.ENVIADA);
            resposta.setDataEnvio(LocalDate.now());
        } else {
            resposta.setStatus(StatusResposta.EM_ANDAMENTO);
        }

        return Response.ok(paraStatusDTO(resposta)).build();
    }

    private RespostaInstrumento buscarResposta(UUID instrumentoId, String token) {
        List<RespostaInstrumento> existentes = em.createQuery(
                        "select r from RespostaInstrumento r where r.instrumentoAvaliativo.id = :iid and r.usuarioRef = :ref",
                        RespostaInstrumento.class)
                .setParameter("iid", instrumentoId)
                .setParameter("ref", token)
                .getResultList();
        return existentes.isEmpty() ? null : existentes.get(0);
    }

    private RespostaQuestao buscarOuCriarRespostaQuestao(RespostaInstrumento resposta, SessaoQuestao sq) {
        List<RespostaQuestao> existentes = em.createQuery(
                        "select rq from RespostaQuestao rq where rq.respostaInstrumento.id = :rid and rq.sessaoQuestao.id = :sqid",
                        RespostaQuestao.class)
                .setParameter("rid", resposta.getId())
                .setParameter("sqid", sq.getId())
                .getResultList();
        if (!existentes.isEmpty()) {
            return existentes.get(0);
        }
        RespostaQuestao rq = new RespostaQuestao();
        rq.setRespostaInstrumento(resposta);
        rq.setSessaoQuestao(sq);
        em.persist(rq);
        return rq;
    }

    private static RespostaItemDTO buscarItem(RespostaEnvioDTO envio, UUID questaoId) {
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
                        rq.getSessaoQuestao().getQuestao().getId(),
                        rq.getOpcaoQuestao() != null ? rq.getOpcaoQuestao().getId() : null,
                        rq.getTexto()))
                .toList();
        return new RespostaStatusDTO(resposta.getStatus().name(), respostas);
    }

    private static InstrumentoRespostaDTO paraDTO(InstrumentoAvaliativo instrumento) {
        List<GrupoDTO> grupos = instrumento.getSessoes().stream()
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

    private static GrupoDTO paraDTO(InstrumentoSessao sessao) {
        List<QuestaoDTO> questoes = sessao.getQuestoes().stream()
                .map(AvaliacaoRespostaResource::paraDTO)
                .toList();
        return new GrupoDTO(sessao.getId(), sessao.getTitulo(), sessao.getDescricao(), questoes);
    }

    private static QuestaoDTO paraDTO(SessaoQuestao sq) {
        Questao questao = sq.getQuestao();
        List<OpcaoDTO> opcoes = questao.getConjuntoOpcao() != null
                ? questao.getConjuntoOpcao().getOpcoes().stream()
                    .map(o -> new OpcaoDTO(o.getId(), o.getTexto()))
                    .toList()
                : List.of();
        return new QuestaoDTO(questao.getId(), questao.getEnunciado(), questao.getTipo().name(),
                sq.isObrigatoria(), opcoes);
    }
}
