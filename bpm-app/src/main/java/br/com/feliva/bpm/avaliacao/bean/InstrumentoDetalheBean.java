package br.com.feliva.bpm.avaliacao.bean;

import br.com.feliva.bpm.avaliacao.model.AreaConhecimento;
import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.com.feliva.bpm.avaliacao.model.InstrumentoSessao;
import br.com.feliva.bpm.avaliacao.model.OpcaoQuestao;
import br.com.feliva.bpm.avaliacao.model.Questao;
import br.com.feliva.bpm.avaliacao.model.SessaoQuestao;
import br.com.feliva.bpm.avaliacao.model.TipoQuestao;
import br.com.feliva.bpm.avaliacao.model.TipoSessao;
import br.com.feliva.bpm.avaliacao.dao.AreaConhecimentoDAO;
import br.com.feliva.bpm.avaliacao.dao.InstrumentoAvaliacaoDAO;
import br.com.feliva.bpm.avaliacao.dao.InstrumentoSessaoDAO;
import br.com.feliva.bpm.avaliacao.dao.OpcaoQuestaoDAO;
import br.com.feliva.bpm.avaliacao.dao.QuestaoDAO;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerenciamento visual da estrutura do Instrumento Avaliativo:
 * controle de páginas/sessões, questões vinculadas e opções de resposta.
 */
@Named
@ViewScoped
public class InstrumentoDetalheBean implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(InstrumentoDetalheBean.class.getName());

    @Inject
    private InstrumentoAvaliacaoDAO instrumentoDAO;

    @Inject
    private InstrumentoSessaoDAO sessaoDAO;

    @Inject
    private QuestaoDAO questaoDAO;

    @Inject
    private OpcaoQuestaoDAO opcaoDAO;

    @Inject
    private AreaConhecimentoDAO areaConhecimentoDAO;

    private UUID instrumentoId;
    private InstrumentoAvaliativo instrumento;

    // Estados dos modais / formulários inline
    private boolean editandoSessao = false;
    private InstrumentoSessao sessaoEmEdicao;

    private boolean editandoQuestao = false;
    private InstrumentoSessao sessaoSelecionada;
    private SessaoQuestao sessaoQuestaoEmEdicao;
    private Questao questaoEmEdicao;

    private boolean gerenciandoOpcoes = false;
    private SessaoQuestao sessaoQuestaoOpcoes;
    private String novaOpcaoTexto;
    private BigDecimal novaOpcaoValor;

    // Modo pré-visualização ao vivo
    private boolean previsualizar = false;

    public void carregar() {
        if (instrumentoId == null) {
            return;
        }
        try {
            instrumento = instrumentoDAO.buscarComEstruturaCompleta(instrumentoId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar instrumento " + instrumentoId, e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar instrumento: " + e.getMessage(), null));
        }
    }

    /* -------------------------------------------------------------------------
     * Estatísticas do Instrumento
     * ------------------------------------------------------------------------- */

    public int getTotalSessoes() {
        return (instrumento != null && instrumento.getSessoes() != null) ? instrumento.getSessoes().size() : 0;
    }

    public int getTotalQuestoes() {
        if (instrumento == null || instrumento.getSessoes() == null) {
            return 0;
        }
        int total = 0;
        for (InstrumentoSessao s : instrumento.getSessoes()) {
            if (s.getQuestoes() != null) {
                total += s.getQuestoes().size();
            }
        }
        return total;
    }

    public int getTotalObrigatorias() {
        if (instrumento == null || instrumento.getSessoes() == null) {
            return 0;
        }
        int total = 0;
        for (InstrumentoSessao s : instrumento.getSessoes()) {
            if (s.getQuestoes() != null) {
                for (SessaoQuestao sq : s.getQuestoes()) {
                    if (sq.isObrigatoria()) {
                        total++;
                    }
                }
            }
        }
        return total;
    }

    /* -------------------------------------------------------------------------
     * Gerenciamento de Sessão / Página
     * ------------------------------------------------------------------------- */

    public void novaSessao() {
        sessaoEmEdicao = new InstrumentoSessao();
        sessaoEmEdicao.setInstrumentoAvaliativo(instrumento);
        sessaoEmEdicao.setTipo(TipoSessao.PAGINA);
        short proximaOrdem = (short) (getTotalSessoes() + 1);
        sessaoEmEdicao.setOrdem(proximaOrdem);
        editandoSessao = true;
        editandoQuestao = false;
        gerenciandoOpcoes = false;
    }

    public void editarSessao(InstrumentoSessao s) {
        this.sessaoEmEdicao = s;
        this.editandoSessao = true;
        this.editandoQuestao = false;
        this.gerenciandoOpcoes = false;
    }

    public void salvarSessao() {
        try {
            if (sessaoEmEdicao.getTitulo() == null || sessaoEmEdicao.getTitulo().isBlank()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "O título da sessão é obrigatório.", null));
                return;
            }
            sessaoEmEdicao.setInstrumentoAvaliativo(instrumento);
            sessaoDAO.salvar(sessaoEmEdicao);
            editandoSessao = false;
            carregar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sessão salva com sucesso!", null));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar sessao", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar sessão: " + e.getMessage(), null));
        }
    }

    public void excluirSessao(InstrumentoSessao s) {
        try {
            sessaoDAO.excluir(s);
            carregar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sessão removida.", null));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir sessao", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir sessão: " + e.getMessage(), null));
        }
    }

    public void moverSessaoCima(InstrumentoSessao s) {
        ajustarOrdemSessao(s, -1);
    }

    public void moverSessaoBaixo(InstrumentoSessao s) {
        ajustarOrdemSessao(s, 1);
    }

    private void ajustarOrdemSessao(InstrumentoSessao s, int delta) {
        try {
            sessaoDAO.reordenar(instrumento != null ? instrumento.getSessoes() : null, s, delta);
            carregar();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao ajustar ordem da sessao", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao alterar ordem da sessão: " + e.getMessage(), null));
        }
    }

    /* -------------------------------------------------------------------------
     * Gerenciamento de Questão
     * ------------------------------------------------------------------------- */

    public void novaQuestao(InstrumentoSessao s) {
        this.sessaoSelecionada = s;
        this.questaoEmEdicao = new Questao();
        this.questaoEmEdicao.setTipo(TipoQuestao.ESCOLHA_UNICA);

        this.sessaoQuestaoEmEdicao = new SessaoQuestao();
        this.sessaoQuestaoEmEdicao.setInstrumentoSessao(s);
        this.sessaoQuestaoEmEdicao.setObrigatoria(true);
        short proximaOrdem = (short) (s.getQuestoes() != null ? s.getQuestoes().size() + 1 : 1);
        this.sessaoQuestaoEmEdicao.setOrdem(proximaOrdem);

        this.editandoQuestao = true;
        this.editandoSessao = false;
        this.gerenciandoOpcoes = false;
    }

    public void editarQuestao(SessaoQuestao sq) {
        this.sessaoQuestaoEmEdicao = sq;
        this.questaoEmEdicao = sq.getQuestao();
        this.sessaoSelecionada = sq.getInstrumentoSessao();
        this.editandoQuestao = true;
        this.editandoSessao = false;
        this.gerenciandoOpcoes = false;
    }

    public void salvarQuestao() {
        try {
            if (questaoEmEdicao.getEnunciado() == null || questaoEmEdicao.getEnunciado().isBlank()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "O enunciado da questão é obrigatório.", null));
                return;
            }

            questaoDAO.salvarQuestao(sessaoSelecionada, sessaoQuestaoEmEdicao, questaoEmEdicao);
            editandoQuestao = false;
            carregar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Questão salva com sucesso!", null));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar questao", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar questão: " + e.getMessage(), null));
        }
    }

    public void excluirQuestao(SessaoQuestao sq) {
        try {
            if (sq == null) {
                LOGGER.warning("excluirQuestao chamada com sq nulo");
                return;
            }
            LOGGER.info("Excluindo questao: " + sq.getId());
            questaoDAO.excluirSessaoQuestao(sq, instrumento);
            carregar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Questão removida.", null));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir questao", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir questão: " + e.getMessage(), null));
        }
    }

    public void moverQuestaoCima(SessaoQuestao sq) {
        ajustarOrdemQuestao(sq, -1);
    }

    public void moverQuestaoBaixo(SessaoQuestao sq) {
        ajustarOrdemQuestao(sq, 1);
    }

    private void ajustarOrdemQuestao(SessaoQuestao sq, int delta) {
        try {
            InstrumentoSessao s = sq.getInstrumentoSessao();
            questaoDAO.reordenar(s != null ? s.getQuestoes() : null, sq, delta);
            carregar();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao ajustar ordem da questao", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao alterar ordem da questão: " + e.getMessage(), null));
        }
    }

    /* -------------------------------------------------------------------------
     * Gerenciamento de Opções de Resposta
     * ------------------------------------------------------------------------- */

    public void abrirGerenciadorOpcoes(SessaoQuestao sq) {
        this.sessaoQuestaoOpcoes = sq;
        this.novaOpcaoTexto = null;
        this.novaOpcaoValor = null;
        this.gerenciandoOpcoes = true;
        this.editandoSessao = false;
        this.editandoQuestao = false;
    }

    public void adicionarOpcao() {
        try {
            if (novaOpcaoTexto == null || novaOpcaoTexto.isBlank()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Informe o texto da opção.", null));
                return;
            }
            Questao q = sessaoQuestaoOpcoes.getQuestao();
            opcaoDAO.adicionarOpcao(q, novaOpcaoTexto, novaOpcaoValor);

            novaOpcaoTexto = null;
            novaOpcaoValor = null;
            carregar();
            sincronizarSessaoQuestaoOpcoes();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar opcao", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao adicionar opção: " + e.getMessage(), null));
        }
    }

    public void excluirOpcao(OpcaoQuestao opcao) {
        try {
            if (opcao == null) {
                LOGGER.warning("excluirOpcao chamada com opcao nula");
                return;
            }
            opcaoDAO.excluirOpcao(opcao);
            carregar();
            sincronizarSessaoQuestaoOpcoes();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir opcao", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir opção: " + e.getMessage(), null));
        }
    }

    public void aplicarTemplateLikert5() {
        aplicarTemplate(new String[]{
                "Discordo totalmente",
                "Discordo parcialmente",
                "Neutro / Indiferente",
                "Concordo parcialmente",
                "Concordo totalmente"
        }, new BigDecimal[]{
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(3.0),
                BigDecimal.valueOf(4.0),
                BigDecimal.valueOf(5.0)
        });
    }

    public void aplicarTemplateQualidade() {
        aplicarTemplate(new String[]{
                "Insuficiente",
                "Regular",
                "Bom",
                "Excelente"
        }, new BigDecimal[]{
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(3.0),
                BigDecimal.valueOf(4.0)
        });
    }

    public void aplicarTemplateSimNao() {
        aplicarTemplate(new String[]{
                "Sim",
                "Não"
        }, new BigDecimal[]{
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(0.0)
        });
    }

    private void aplicarTemplate(String[] textos, BigDecimal[] valores) {
        try {
            if (sessaoQuestaoOpcoes == null) {
                return;
            }
            Questao q = sessaoQuestaoOpcoes.getQuestao();
            opcaoDAO.aplicarTemplate(q, textos, valores);
            carregar();
            sincronizarSessaoQuestaoOpcoes();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Template de opções aplicado com sucesso!", null));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao aplicar template de opcoes", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao aplicar template: " + e.getMessage(), null));
        }
    }

    private void sincronizarSessaoQuestaoOpcoes() {
        if (sessaoQuestaoOpcoes == null || instrumento == null || instrumento.getSessoes() == null) {
            return;
        }
        for (InstrumentoSessao s : instrumento.getSessoes()) {
            if (s.getQuestoes() != null) {
                for (SessaoQuestao item : s.getQuestoes()) {
                    if (item.getId().equals(sessaoQuestaoOpcoes.getId())) {
                        sessaoQuestaoOpcoes = item;
                        return;
                    }
                }
            }
        }
    }

    /* -------------------------------------------------------------------------
     * Modos e Controles de Tela
     * ------------------------------------------------------------------------- */

    public void alternarPreview() {
        this.previsualizar = !this.previsualizar;
        fecharModal();
    }

    public void fecharModal() {
        this.editandoSessao = false;
        this.editandoQuestao = false;
        this.gerenciandoOpcoes = false;
    }

    public List<AreaConhecimento> completeAreaConhecimento(String query) {
        try {
            return areaConhecimentoDAO.pesquisar(query, 20);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao autocompletar área do conhecimento com termo: " + query, e);
            return Collections.emptyList();
        }
    }

    public TipoQuestao[] getTiposQuestao() {
        return TipoQuestao.values();
    }

    public TipoSessao[] getTiposSessao() {
        return TipoSessao.values();
    }

    // Getters e Setters

    public UUID getInstrumentoId() {
        return instrumentoId;
    }

    public void setInstrumentoId(UUID instrumentoId) {
        this.instrumentoId = instrumentoId;
        carregar();
    }

    public InstrumentoAvaliativo getInstrumento() {
        return instrumento;
    }

    public boolean isEditandoSessao() {
        return editandoSessao;
    }

    public InstrumentoSessao getSessaoEmEdicao() {
        return sessaoEmEdicao;
    }

    public boolean isEditandoQuestao() {
        return editandoQuestao;
    }

    public InstrumentoSessao getSessaoSelecionada() {
        return sessaoSelecionada;
    }

    public SessaoQuestao getSessaoQuestaoEmEdicao() {
        return sessaoQuestaoEmEdicao;
    }

    public Questao getQuestaoEmEdicao() {
        return questaoEmEdicao;
    }

    public boolean isGerenciandoOpcoes() {
        return gerenciandoOpcoes;
    }

    public SessaoQuestao getSessaoQuestaoOpcoes() {
        return sessaoQuestaoOpcoes;
    }

    public String getNovaOpcaoTexto() {
        return novaOpcaoTexto;
    }

    public void setNovaOpcaoTexto(String novaOpcaoTexto) {
        this.novaOpcaoTexto = novaOpcaoTexto;
    }

    public BigDecimal getNovaOpcaoValor() {
        return novaOpcaoValor;
    }

    public void setNovaOpcaoValor(BigDecimal novaOpcaoValor) {
        this.novaOpcaoValor = novaOpcaoValor;
    }

    public boolean isPrevisualizar() {
        return previsualizar;
    }
}
