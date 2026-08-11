package br.edu.iffar.bpm.avaliacao.bean;

import br.edu.iffar.bpm.avaliacao.model.GrupoQuestao;
import br.edu.iffar.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.edu.iffar.bpm.avaliacao.model.OpcaoQuestao;
import br.edu.iffar.bpm.avaliacao.model.Questao;
import br.edu.iffar.bpm.avaliacao.model.TipoQuestao;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;

/**
 * Edição do conteúdo (grupos, questões e opções) de um instrumento já
 * cadastrado. O cadastro do instrumento em si (título, período, etc.) é
 * feito em InstrumentoBean.
 */
@Named
@ViewScoped
@Transactional
public class InstrumentoDetalheBean implements Serializable {

    @Inject
    private EntityManager em;

    private Long instrumentoId;

    private GrupoQuestao novoGrupo = new GrupoQuestao();

    private Long grupoSelecionadoId;
    private Questao novaQuestao = new Questao();

    private Long questaoSelecionadaId;
    private OpcaoQuestao novaOpcao = new OpcaoQuestao();

    public Long getInstrumentoId() {
        return instrumentoId;
    }

    public void setInstrumentoId(Long instrumentoId) {
        this.instrumentoId = instrumentoId;
    }

    public InstrumentoAvaliativo getInstrumento() {
        return em.find(InstrumentoAvaliativo.class, instrumentoId);
    }

    public GrupoQuestao getNovoGrupo() {
        return novoGrupo;
    }

    public String salvarGrupo() {
        InstrumentoAvaliativo instrumento = getInstrumento();
        novoGrupo.setInstrumento(instrumento);
        novoGrupo.setOrdem(instrumento.getGrupos().size());
        em.persist(novoGrupo);
        novoGrupo = new GrupoQuestao();
        return null;
    }

    public void excluirGrupo(GrupoQuestao grupo) {
        em.remove(em.merge(grupo));
    }

    public Long getGrupoSelecionadoId() {
        return grupoSelecionadoId;
    }

    public void setGrupoSelecionadoId(Long grupoSelecionadoId) {
        this.grupoSelecionadoId = grupoSelecionadoId;
    }

    public Questao getNovaQuestao() {
        return novaQuestao;
    }

    public TipoQuestao[] getTiposQuestao() {
        return TipoQuestao.values();
    }

    public String salvarQuestao() {
        GrupoQuestao grupo = em.find(GrupoQuestao.class, grupoSelecionadoId);
        novaQuestao.setGrupo(grupo);
        novaQuestao.setOrdem(grupo.getQuestoes().size());
        em.persist(novaQuestao);
        novaQuestao = new Questao();
        return null;
    }

    public void excluirQuestao(Questao questao) {
        em.remove(em.merge(questao));
    }

    public Long getQuestaoSelecionadaId() {
        return questaoSelecionadaId;
    }

    public void setQuestaoSelecionadaId(Long questaoSelecionadaId) {
        this.questaoSelecionadaId = questaoSelecionadaId;
    }

    public OpcaoQuestao getNovaOpcao() {
        return novaOpcao;
    }

    public String salvarOpcao() {
        Questao questao = em.find(Questao.class, questaoSelecionadaId);
        novaOpcao.setQuestao(questao);
        novaOpcao.setOrdem(questao.getOpcoes().size());
        em.persist(novaOpcao);
        novaOpcao = new OpcaoQuestao();
        return null;
    }

    public void excluirOpcao(OpcaoQuestao opcao) {
        em.remove(em.merge(opcao));
    }
}
