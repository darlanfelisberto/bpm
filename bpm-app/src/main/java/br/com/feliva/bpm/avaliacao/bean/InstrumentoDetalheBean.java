package br.com.feliva.bpm.avaliacao.bean;

import br.com.feliva.bpm.avaliacao.model.ConjuntoOpcao;
import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.com.feliva.bpm.avaliacao.model.InstrumentoSessao;
import br.com.feliva.bpm.avaliacao.model.OpcaoQuestao;
import br.com.feliva.bpm.avaliacao.model.Questao;
import br.com.feliva.bpm.avaliacao.model.SessaoQuestao;
import br.com.feliva.bpm.avaliacao.model.TipoQuestao;
import br.com.feliva.bpm.avaliacao.model.TipoSessao;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.UUID;

/**
 * Edição do conteúdo (sessões, questões e opções) de um instrumento já
 * cadastrado. O cadastro do instrumento em si (título, período, etc.) é
 * feito em InstrumentoBean.
 */
@Named
@ViewScoped
@Transactional
public class InstrumentoDetalheBean implements Serializable {

    @Inject
    private EntityManager em;

    private UUID instrumentoId;

    private InstrumentoSessao novoGrupo = new InstrumentoSessao();
    private UUID grupoSelecionadoId;

    private Questao novaQuestao = new Questao();
    private boolean novaQuestaoObrigatoria = true;
    private UUID questaoSelecionadaId;

    private OpcaoQuestao novaOpcao = new OpcaoQuestao();

    public UUID getInstrumentoId() {
        return instrumentoId;
    }

    public void setInstrumentoId(UUID instrumentoId) {
        this.instrumentoId = instrumentoId;
    }

    public InstrumentoAvaliativo getInstrumento() {
        return em.find(InstrumentoAvaliativo.class, instrumentoId);
    }

    public InstrumentoSessao getNovoGrupo() {
        return novoGrupo;
    }

    public String salvarGrupo() {
        InstrumentoAvaliativo instrumento = getInstrumento();
        novoGrupo.setInstrumentoAvaliativo(instrumento);
        novoGrupo.setTipo(TipoSessao.PAGINA);
        novoGrupo.setOrdem((short) instrumento.getSessoes().size());
        em.persist(novoGrupo);
        novoGrupo = new InstrumentoSessao();
        return null;
    }

    public void excluirGrupo(InstrumentoSessao grupo) {
        em.remove(em.merge(grupo));
    }

    public UUID getGrupoSelecionadoId() {
        return grupoSelecionadoId;
    }

    public void setGrupoSelecionadoId(UUID grupoSelecionadoId) {
        this.grupoSelecionadoId = grupoSelecionadoId;
    }

    public Questao getNovaQuestao() {
        return novaQuestao;
    }

    public boolean isNovaQuestaoObrigatoria() {
        return novaQuestaoObrigatoria;
    }

    public void setNovaQuestaoObrigatoria(boolean novaQuestaoObrigatoria) {
        this.novaQuestaoObrigatoria = novaQuestaoObrigatoria;
    }

    public TipoQuestao[] getTiposQuestao() {
        return TipoQuestao.values();
    }

    public String salvarQuestao() {
        InstrumentoSessao grupo = em.find(InstrumentoSessao.class, grupoSelecionadoId);
        novaQuestao.setCriadoPor("admin");
        if (novaQuestao.getTipo() != TipoQuestao.DESCRITIVA && novaQuestao.getConjuntoOpcao() == null) {
            ConjuntoOpcao conjunto = new ConjuntoOpcao();
            String prefixo = novaQuestao.getEnunciado() != null && novaQuestao.getEnunciado().length() > 30
                    ? novaQuestao.getEnunciado().substring(0, 30)
                    : (novaQuestao.getEnunciado() != null ? novaQuestao.getEnunciado() : "Opções");
            conjunto.setNome(prefixo + " (" + UUID.randomUUID().toString().substring(0, 8) + ")");
            em.persist(conjunto);
            novaQuestao.setConjuntoOpcao(conjunto);
        }
        em.persist(novaQuestao);

        SessaoQuestao sq = new SessaoQuestao();
        sq.setInstrumentoSessao(grupo);
        sq.setQuestao(novaQuestao);
        sq.setObrigatoria(novaQuestaoObrigatoria);
        sq.setOrdem((short) grupo.getQuestoes().size());
        em.persist(sq);

        novaQuestao = new Questao();
        novaQuestaoObrigatoria = true;
        return null;
    }

    public void excluirQuestao(SessaoQuestao sq) {
        em.remove(em.merge(sq));
    }

    public UUID getQuestaoSelecionadaId() {
        return questaoSelecionadaId;
    }

    public void setQuestaoSelecionadaId(UUID questaoSelecionadaId) {
        this.questaoSelecionadaId = questaoSelecionadaId;
    }

    public OpcaoQuestao getNovaOpcao() {
        return novaOpcao;
    }

    public String salvarOpcao() {
        Questao questao = em.find(Questao.class, questaoSelecionadaId);
        ConjuntoOpcao conjunto = questao.getConjuntoOpcao();
        if (conjunto == null) {
            conjunto = new ConjuntoOpcao();
            conjunto.setNome("Opções - " + questao.getId());
            em.persist(conjunto);
            questao.setConjuntoOpcao(conjunto);
        }
        novaOpcao.setConjuntoOpcao(conjunto);
        novaOpcao.setOrdem((short) conjunto.getOpcoes().size());
        em.persist(novaOpcao);
        novaOpcao = new OpcaoQuestao();
        return null;
    }

    public void excluirOpcao(OpcaoQuestao opcao) {
        em.remove(em.merge(opcao));
    }
}
