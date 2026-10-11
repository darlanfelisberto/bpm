package br.com.feliva.bpm.avaliacao.bean;

import br.com.feliva.bpm.avaliacao.model.ConjuntoOpcao;
import br.com.feliva.bpm.avaliacao.model.InstrumentoAvaliativo;
import br.com.feliva.bpm.avaliacao.model.InstrumentoSessao;
import br.com.feliva.bpm.avaliacao.model.OpcaoQuestao;
import br.com.feliva.bpm.avaliacao.model.Questao;
import br.com.feliva.bpm.avaliacao.model.SessaoQuestao;
import br.com.feliva.bpm.avaliacao.model.TipoQuestao;
import br.com.feliva.bpm.avaliacao.model.TipoSessao;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
@Transactional
public class InstrumentoDetalheBean implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(InstrumentoDetalheBean.class.getName());

    @Inject
    private EntityManager em;

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
            instrumento = em.find(InstrumentoAvaliativo.class, instrumentoId);
            if (instrumento != null) {
                List<InstrumentoSessao> sessoes = em.createQuery(
                        "select distinct s from InstrumentoSessao s " +
                        "left join fetch s.questoes sq " +
                        "left join fetch sq.questao q " +
                        "left join fetch q.conjuntoOpcao co " +
                        "where s.instrumentoAvaliativo.instrumentoAvaliativoId = :id " +
                        "order by s.ordem asc", InstrumentoSessao.class)
                        .setParameter("id", instrumentoId)
                        .getResultList();

                for (InstrumentoSessao s : sessoes) {
                    if (s.getQuestoes() != null) {
                        for (SessaoQuestao sq : s.getQuestoes()) {
                            if (sq.getQuestao() != null && sq.getQuestao().getConjuntoOpcao() != null) {
                                sq.getQuestao().getConjuntoOpcao().getOpcoes().size();
                            }
                        }
                    }
                }
                instrumento.setSessoes(sessoes);
            }
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
            if (sessaoEmEdicao.getId() == null) {
                em.persist(sessaoEmEdicao);
            } else {
                sessaoEmEdicao = em.merge(sessaoEmEdicao);
            }
            em.flush();
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
            InstrumentoSessao managed = em.contains(s) ? s : em.find(InstrumentoSessao.class, s.getId());
            if (managed != null) {
                em.remove(managed);
            }
            em.flush();
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
        List<InstrumentoSessao> lista = instrumento.getSessoes();
        int idx = lista.indexOf(s);
        int targetIdx = idx + delta;
        if (idx >= 0 && targetIdx >= 0 && targetIdx < lista.size()) {
            InstrumentoSessao vizinho = lista.get(targetIdx);
            short ordemOriginal = s.getOrdem();
            s.setOrdem(vizinho.getOrdem());
            vizinho.setOrdem(ordemOriginal);
            em.merge(s);
            em.merge(vizinho);
            em.flush();
            carregar();
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

            if (questaoEmEdicao.getId() == null) {
                questaoEmEdicao.setCriadoPor("admin");
                questaoEmEdicao.setCriadoEm(OffsetDateTime.now());

                if (questaoEmEdicao.getTipo() != TipoQuestao.DESCRITIVA && questaoEmEdicao.getConjuntoOpcao() == null) {
                    ConjuntoOpcao co = new ConjuntoOpcao();
                    String prefixo = questaoEmEdicao.getEnunciado().length() > 30
                            ? questaoEmEdicao.getEnunciado().substring(0, 30)
                            : questaoEmEdicao.getEnunciado();
                    co.setNome(prefixo.trim() + " (" + UUID.randomUUID().toString().substring(0, 8) + ")");
                    em.persist(co);
                    questaoEmEdicao.setConjuntoOpcao(co);
                }
                em.persist(questaoEmEdicao);

                sessaoQuestaoEmEdicao.setQuestao(questaoEmEdicao);
                sessaoQuestaoEmEdicao.setInstrumentoSessao(sessaoSelecionada);
                em.persist(sessaoQuestaoEmEdicao);
            } else {
                if (questaoEmEdicao.getTipo() != TipoQuestao.DESCRITIVA && questaoEmEdicao.getConjuntoOpcao() == null) {
                    ConjuntoOpcao co = new ConjuntoOpcao();
                    co.setNome("Opções (" + UUID.randomUUID().toString().substring(0, 8) + ")");
                    em.persist(co);
                    questaoEmEdicao.setConjuntoOpcao(co);
                } else if (questaoEmEdicao.getTipo() == TipoQuestao.DESCRITIVA) {
                    questaoEmEdicao.setConjuntoOpcao(null);
                }
                questaoEmEdicao = em.merge(questaoEmEdicao);
                sessaoQuestaoEmEdicao = em.merge(sessaoQuestaoEmEdicao);
            }

            em.flush();
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
            }
            em.flush();
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
        InstrumentoSessao s = sq.getInstrumentoSessao();
        List<SessaoQuestao> lista = s.getQuestoes();
        int idx = lista.indexOf(sq);
        int targetIdx = idx + delta;
        if (idx >= 0 && targetIdx >= 0 && targetIdx < lista.size()) {
            SessaoQuestao vizinho = lista.get(targetIdx);
            short ordemOriginal = sq.getOrdem();
            sq.setOrdem(vizinho.getOrdem());
            vizinho.setOrdem(ordemOriginal);
            em.merge(sq);
            em.merge(vizinho);
            em.flush();
            carregar();
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
            ConjuntoOpcao conjunto = q.getConjuntoOpcao();
            if (conjunto == null) {
                conjunto = new ConjuntoOpcao();
                conjunto.setNome("Opções (" + UUID.randomUUID().toString().substring(0, 8) + ")");
                em.persist(conjunto);
                q.setConjuntoOpcao(conjunto);
                em.merge(q);
            }
            OpcaoQuestao op = new OpcaoQuestao();
            op.setConjuntoOpcao(conjunto);
            op.setTexto(novaOpcaoTexto.trim());
            op.setValor(novaOpcaoValor);
            short proximaOrdem = (short) (conjunto.getOpcoes() != null ? conjunto.getOpcoes().size() + 1 : 1);
            op.setOrdem(proximaOrdem);
            em.persist(op);
            em.flush();

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
            OpcaoQuestao managed = em.contains(opcao) ? opcao : em.find(OpcaoQuestao.class, opcao.getId());
            if (managed != null) {
                if (managed.getConjuntoOpcao() != null && managed.getConjuntoOpcao().getOpcoes() != null) {
                    managed.getConjuntoOpcao().getOpcoes().remove(managed);
                }
                em.remove(managed);
            }
            em.flush();
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
            ConjuntoOpcao conjunto = q.getConjuntoOpcao();
            if (conjunto == null) {
                conjunto = new ConjuntoOpcao();
                conjunto.setNome("Opções (" + UUID.randomUUID().toString().substring(0, 8) + ")");
                em.persist(conjunto);
                q.setConjuntoOpcao(conjunto);
                em.merge(q);
            }
            for (int i = 0; i < textos.length; i++) {
                OpcaoQuestao op = new OpcaoQuestao();
                op.setConjuntoOpcao(conjunto);
                op.setTexto(textos[i]);
                op.setValor(valores != null && i < valores.length ? valores[i] : null);
                op.setOrdem((short) (i + 1));
                em.persist(op);
            }
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
