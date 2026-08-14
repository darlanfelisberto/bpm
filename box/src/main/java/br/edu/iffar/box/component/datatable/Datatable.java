package br.edu.iffar.box.component.datatable;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabela com paginação/ordenação/filtro sempre resolvidos no backend - cada
 * interação (clicar numa página, num cabeçalho ordenável, digitar num
 * filtro) dispara um postback que chama de novo
 * {@link DatatableLazyModel#carregar(DatatableConsulta)}. Nenhuma linha
 * fica guardada entre requisições: só primitivos de controle (página,
 * campo/direção de ordenação, filtros) vão pro estado da view.
 *
 * Client behaviors "page", "sort" e "filter" (sempre auto-renderizam a
 * própria tabela, mesmo sem um &lt;f:ajax&gt; explícito, porque sem isso a
 * tabela nunca mostraria o resultado da interação) e "select" (clique numa
 * linha - evento default, igual ao b:schedule2 - informativo, não
 * auto-renderiza nada por padrão).
 *
 * Seleção de linha não guarda o objeto: só o id (via um Converter) trafega
 * no request, igual ao campo transiente "eventoId" do b:schedule2. Um
 * getter de conveniência (getObjetoSelecionado) usa o Converter pra
 * reconstruir o objeto sob demanda, sem nada ficar no StateHelper.
 *
 * Facets opcionais "header"/"footer" (bloco acima/abaixo da tabela, ex.:
 * título, barra de ações) e "empty" (substitui o corpo da tabela quando a
 * página atual não tem linha nenhuma - normalmente por causa de um filtro).
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <b:datatable value="#{bean.modelo}" var="item" linhasPorPagina="20"
 *                    converter="#{bean.conversorDeId}">
 *          <f:facet name="header">Título da tabela</f:facet>
 *          <b:column field="nome" header="Nome" sortable="true" filterable="true"/>
 *          <b:column field="email" header="E-mail" filterable="true"/>
 *          <b:column header="Status">
 *              <span class="badge">#{item.status}</span>
 *          </b:column>
 *          <f:facet name="empty">Nenhum registro encontrado.</f:facet>
 *          <f:ajax event="select" listener="#{bean.aoSelecionar}" render=":resultado"/>
 *      </b:datatable>
 */
@FacesComponent(
        value = Datatable.COMPONENT_TYPE,
        createTag = true,
        tagName = "datatable",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "datatable/datatable.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "datatable/datatable.js", target = "head")
})
public class Datatable extends UIComponentBase implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Datatable";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Datatable";

    private static final List<String> EVENT_NAMES =
            Collections.unmodifiableList(List.of("page", "sort", "filter", "select"));

    private final Map<String, List<ClientBehavior>> comportamentos = new HashMap<>();

    private transient String dadoLinhaId;

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public Map<String, List<ClientBehavior>> getClientBehaviors() {
        return Collections.unmodifiableMap(comportamentos);
    }

    @Override
    public void addClientBehavior(String eventName, ClientBehavior behavior) {
        comportamentos.computeIfAbsent(eventName, k -> new ArrayList<>()).add(behavior);
    }

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return "select";
    }

    @SuppressWarnings("unchecked")
    public DatatableLazyModel<Object> getValue() {
        return (DatatableLazyModel<Object>) getStateHelper().eval("value");
    }

    public void setValue(DatatableLazyModel<Object> value) {
        getStateHelper().put("value", value);
    }

    /** Nome que expõe a linha atual em EL pros filhos das colunas (igual ao "var" do h:dataTable). */
    public String getVar() {
        return (String) getStateHelper().eval("var");
    }

    public void setVar(String var) {
        getStateHelper().put("var", var);
    }

    /** Opcional - sem valor (ou <= 0), usa {@link DatatableLazyModel#linhasPorPaginaPadrao()}. */
    public Integer getLinhasPorPagina() {
        return (Integer) getStateHelper().eval("linhasPorPagina");
    }

    public void setLinhasPorPagina(Integer linhasPorPagina) {
        getStateHelper().put("linhasPorPagina", linhasPorPagina);
    }

    /** Necessário só pro evento "select": converte a linha clicada pra um id (ida) e de volta pro objeto (volta). */
    public Converter getConverter() {
        return (Converter) getStateHelper().eval("converter");
    }

    public void setConverter(Converter converter) {
        getStateHelper().put("converter", converter);
    }

    public String getOrdenarPorPadrao() {
        return (String) getStateHelper().eval("ordenarPorPadrao");
    }

    public void setOrdenarPorPadrao(String ordenarPorPadrao) {
        getStateHelper().put("ordenarPorPadrao", ordenarPorPadrao);
    }

    public boolean isOrdenarAscendentePadrao() {
        Boolean valor = (Boolean) getStateHelper().eval("ordenarAscendentePadrao");
        return valor == null || valor;
    }

    public void setOrdenarAscendentePadrao(boolean ordenarAscendentePadrao) {
        getStateHelper().put("ordenarAscendentePadrao", ordenarAscendentePadrao);
    }

    /** Id (via Converter) da linha clicada no evento "select" - null nos demais eventos. */
    public String getLinhaId() {
        return dadoLinhaId;
    }

    /** Reconstrói a linha selecionada a partir do id, via Converter - null sem seleção ou sem Converter configurado. */
    public Object getObjetoSelecionado() {
        if (dadoLinhaId == null) {
            return null;
        }
        Converter conversor = getConverter();
        if (conversor == null) {
            return null;
        }
        return conversor.getAsObject(FacesContext.getCurrentInstance(), this, dadoLinhaId);
    }

    private int paginaAtual() {
        Integer pagina = (Integer) getStateHelper().get("pagina");
        return pagina != null ? pagina : 0;
    }

    private String ordenarPorAtual() {
        String valor = (String) getStateHelper().get("ordenarPor");
        return valor != null ? valor : getOrdenarPorPadrao();
    }

    private boolean ordenarAscendenteAtual() {
        Boolean valor = (Boolean) getStateHelper().get("ordenarAscendente");
        return valor != null ? valor : isOrdenarAscendentePadrao();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> filtrosAtuais() {
        Map<String, String> filtros = (Map<String, String>) getStateHelper().get("filtros");
        return filtros != null ? filtros : Map.of();
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> parametros = context.getExternalContext().getRequestParameterMap();

        if (!clientId.equals(parametros.get("jakarta.faces.source"))) {
            return;
        }
        String nomeEvento = parametros.get("jakarta.faces.behavior.event");
        if (nomeEvento == null) {
            return;
        }

        switch (nomeEvento) {
            case "page" -> decodePagina(parametros, clientId);
            case "sort" -> decodeOrdenacao(parametros, clientId);
            case "filter" -> decodeFiltros(parametros, clientId);
            case "select" -> dadoLinhaId = parametros.get(clientId + "_linhaId");
            default -> { }
        }

        List<ClientBehavior> comportamentosDoEvento = comportamentos.get(nomeEvento);
        if (comportamentosDoEvento != null) {
            for (ClientBehavior comportamento : comportamentosDoEvento) {
                comportamento.decode(context, this);
            }
        }
    }

    private void decodePagina(Map<String, String> parametros, String clientId) {
        Integer pagina = parseInteiro(parametros.get(clientId + "_pagina"));
        if (pagina != null) {
            getStateHelper().put("pagina", Math.max(0, pagina));
        }
    }

    private void decodeOrdenacao(Map<String, String> parametros, String clientId) {
        String campoClicado = parametros.get(clientId + "_ordenarPor");
        if (campoClicado == null || campoClicado.isBlank()) {
            return;
        }
        Ordenacao proxima = proximaOrdenacao(campoClicado, ordenarPorAtual(), ordenarAscendenteAtual());
        getStateHelper().put("ordenarPor", proxima.campo());
        getStateHelper().put("ordenarAscendente", proxima.ascendente());
        getStateHelper().put("pagina", 0);
    }

    private void decodeFiltros(Map<String, String> parametros, String clientId) {
        getStateHelper().put("filtros", new HashMap<>(parseFiltros(parametros.get(clientId + "_filtros"))));
        getStateHelper().put("pagina", 0);
    }

    static Integer parseInteiro(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException erro) {
            return null;
        }
    }

    /** Ciclo nenhum->asc->desc->nenhum ao clicar sempre no mesmo campo; clicar noutro campo sempre volta pra asc. */
    static Ordenacao proximaOrdenacao(String campoClicado, String ordenarPorAtual, boolean ascendenteAtual) {
        if (!campoClicado.equals(ordenarPorAtual)) {
            return new Ordenacao(campoClicado, true);
        }
        if (ascendenteAtual) {
            return new Ordenacao(campoClicado, false);
        }
        return new Ordenacao(null, true);
    }

    /** JSON malformado/adulterado (campo->texto) vira "sem filtro nenhum" em vez de derrubar a requisição. */
    static Map<String, String> parseFiltros(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        Map<String, String> filtros = new LinkedHashMap<>();
        try (JsonReader leitor = Json.createReader(new StringReader(json))) {
            JsonObject objeto = leitor.readObject();
            for (String campo : objeto.keySet()) {
                String valor = objeto.getString(campo, "");
                if (!valor.isBlank()) {
                    filtros.put(campo, valor);
                }
            }
        } catch (RuntimeException erro) {
            return Map.of();
        }
        return filtros;
    }

    /** Corrige uma página que ficou fora do intervalo (ex.: filtro reduziu o total) pra última página válida. */
    static int corrigirPaginaForaDoLimite(int pagina, int linhasPorPagina, long total) {
        if (linhasPorPagina <= 0 || total <= 0) {
            return 0;
        }
        long totalPaginas = (total + linhasPorPagina - 1) / linhasPorPagina;
        long primeiro = (long) pagina * linhasPorPagina;
        if (primeiro < total) {
            return pagina;
        }
        return (int) Math.max(0, totalPaginas - 1);
    }

    record Ordenacao(String campo, boolean ascendente) {
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        String clientId = getClientId(context);

        writer.startElement("div", this);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", "box-datatable", null);
        for (String nomeEvento : EVENT_NAMES) {
            writer.writeAttribute("data-render-" + nomeEvento, renderPara(nomeEvento, clientId), null);
        }
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        List<Column> colunas = colunas();
        DatatableLazyModel<Object> modelo = getValue();
        if (modelo == null || colunas.isEmpty()) {
            return;
        }

        int linhasPorPagina = linhasPorPaginaEfetivo(modelo);
        int pagina = paginaAtual();
        String ordenarPor = ordenarPorAtual();
        boolean ordenarAscendente = ordenarAscendenteAtual();
        Map<String, String> filtros = filtrosAtuais();

        DatatablePage<Object> resultado = modelo.carregar(
                new DatatableConsulta(pagina * linhasPorPagina, linhasPorPagina, ordenarPor, ordenarAscendente, filtros));

        long primeiro = (long) pagina * linhasPorPagina;
        if (resultado.total() > 0 && primeiro >= resultado.total()) {
            pagina = corrigirPaginaForaDoLimite(pagina, linhasPorPagina, resultado.total());
            getStateHelper().put("pagina", pagina);
            resultado = modelo.carregar(
                    new DatatableConsulta(pagina * linhasPorPagina, linhasPorPagina, ordenarPor, ordenarAscendente, filtros));
        }

        ResponseWriter writer = context.getResponseWriter();
        escreverFacet(context, writer, "header", "box-datatable-topo");
        escreverTabela(context, writer, colunas, resultado, ordenarPor, ordenarAscendente, filtros);
        escreverPaginacao(writer, pagina, linhasPorPagina, resultado.total());
        escreverFacet(context, writer, "footer", "box-datatable-rodape");
    }

    private void escreverFacet(FacesContext context, ResponseWriter writer, String nome, String classe) throws IOException {
        UIComponent facet = getFacet(nome);
        if (facet == null || !facet.isRendered()) {
            return;
        }
        writer.startElement("div", this);
        writer.writeAttribute("class", classe, null);
        facet.encodeAll(context);
        writer.endElement("div");
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div");
    }

    private List<Column> colunas() {
        List<Column> colunas = new ArrayList<>();
        for (UIComponent filho : getChildren()) {
            if (filho instanceof Column coluna && filho.isRendered()) {
                colunas.add(coluna);
            }
        }
        return colunas;
    }

    private int linhasPorPaginaEfetivo(DatatableLazyModel<Object> modelo) {
        Integer atributo = getLinhasPorPagina();
        if (atributo != null && atributo > 0) {
            return atributo;
        }
        return modelo.linhasPorPaginaPadrao();
    }

    private void escreverTabela(FacesContext context, ResponseWriter writer, List<Column> colunas,
            DatatablePage<Object> resultado, String ordenarPor, boolean ordenarAscendente, Map<String, String> filtros)
            throws IOException {

        boolean algumaFiltravel = colunas.stream().anyMatch(Column::isFilterable);

        writer.startElement("table", this);
        writer.writeAttribute("class", "box-datatable-tabela", null);

        writer.startElement("thead", this);
        writer.startElement("tr", this);
        writer.writeAttribute("class", "box-datatable-cabecalho", null);
        for (Column coluna : colunas) {
            writer.startElement("th", this);
            String campo = coluna.getField();
            if (coluna.isSortable() && campo != null) {
                writer.writeAttribute("class", "box-datatable-cabecalho-ordenavel", null);
                writer.writeAttribute("data-campo", campo, null);
                if (campo.equals(ordenarPor)) {
                    writer.writeAttribute("data-ordenar-direcao", ordenarAscendente ? "asc" : "desc", null);
                }
            }
            String rotulo = coluna.getHeader() != null ? coluna.getHeader() : campo;
            writer.writeText(rotulo != null ? rotulo : "", "header");
            if (coluna.isSortable() && campo != null && campo.equals(ordenarPor)) {
                writer.startElement("span", this);
                writer.writeAttribute("class", "box-datatable-indicador-ordenacao", null);
                writer.writeText(ordenarAscendente ? " ▲" : " ▼", null);
                writer.endElement("span");
            }
            writer.endElement("th");
        }
        writer.endElement("tr");

        if (algumaFiltravel) {
            writer.startElement("tr", this);
            writer.writeAttribute("class", "box-datatable-filtros", null);
            for (Column coluna : colunas) {
                writer.startElement("td", this);
                String campo = coluna.getField();
                if (coluna.isFilterable() && campo != null) {
                    String rotuloFiltro = coluna.getHeader() != null ? coluna.getHeader() : campo;
                    writer.startElement("input", this);
                    writer.writeAttribute("type", "text", null);
                    writer.writeAttribute("class", "box-datatable-filtro-input", null);
                    writer.writeAttribute("data-campo", campo, null);
                    writer.writeAttribute("placeholder", "Filtrar…", null);
                    writer.writeAttribute("aria-label", "Filtrar por " + rotuloFiltro, null);
                    String valorAtual = filtros.get(campo);
                    if (valorAtual != null) {
                        writer.writeAttribute("value", valorAtual, null);
                    }
                    writer.endElement("input");
                }
                writer.endElement("td");
            }
            writer.endElement("tr");
        }
        writer.endElement("thead");

        writer.startElement("tbody", this);
        String var = getVar();
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        boolean tinhaValorAnterior = var != null && requestMap.containsKey(var);
        Object valorAnterior = tinhaValorAnterior ? requestMap.get(var) : null;
        Converter conversor = getConverter();

        if (resultado.linhas().isEmpty()) {
            escreverLinhaVazia(context, writer, colunas.size());
        }
        for (Object linha : resultado.linhas()) {
            if (var != null) {
                requestMap.put(var, linha);
            }
            writer.startElement("tr", this);
            writer.writeAttribute("class", "box-datatable-linha", null);
            if (conversor != null) {
                writer.writeAttribute("data-linha-id", conversor.getAsString(context, this, linha), null);
            }
            for (Column coluna : colunas) {
                writer.startElement("td", this);
                if (coluna.getChildCount() > 0) {
                    for (UIComponent filho : coluna.getChildren()) {
                        filho.encodeAll(context);
                    }
                } else {
                    String campo = coluna.getField();
                    Object valor = campo != null ? valorDoCampo(context, linha, campo) : null;
                    writer.writeText(valor != null ? valor.toString() : "", null);
                }
                writer.endElement("td");
            }
            writer.endElement("tr");
        }

        if (var != null) {
            if (tinhaValorAnterior) {
                requestMap.put(var, valorAnterior);
            } else {
                requestMap.remove(var);
            }
        }

        writer.endElement("tbody");
        writer.endElement("table");
    }

    private void escreverLinhaVazia(FacesContext context, ResponseWriter writer, int colspan) throws IOException {
        UIComponent vazio = getFacet("empty");
        if (vazio == null || !vazio.isRendered()) {
            return;
        }
        writer.startElement("tr", this);
        writer.writeAttribute("class", "box-datatable-linha-vazia", null);
        writer.startElement("td", this);
        writer.writeAttribute("colspan", String.valueOf(colspan), null);
        vazio.encodeAll(context);
        writer.endElement("td");
        writer.endElement("tr");
    }

    private Object valorDoCampo(FacesContext context, Object linha, String campo) {
        if (linha == null) {
            return null;
        }
        return context.getApplication().getELResolver().getValue(context.getELContext(), linha, campo);
    }

    private void escreverPaginacao(ResponseWriter writer, int pagina, int linhasPorPagina, long total) throws IOException {
        long totalPaginas = total <= 0 ? 1 : (total + linhasPorPagina - 1) / linhasPorPagina;
        long primeiroRegistro = total == 0 ? 0 : (long) pagina * linhasPorPagina + 1;
        long ultimoRegistro = Math.min(total, (long) (pagina + 1) * linhasPorPagina);

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-datatable-paginacao", null);
        writer.writeAttribute("data-pagina-atual", String.valueOf(pagina), null);
        writer.writeAttribute("data-total-paginas", String.valueOf(totalPaginas), null);

        escreverBotaoPaginacao(writer, "primeira", "«", pagina <= 0);
        escreverBotaoPaginacao(writer, "anterior", "‹", pagina <= 0);

        writer.startElement("span", this);
        writer.writeAttribute("class", "box-datatable-paginacao-info", null);
        writer.writeText(total == 0 ? "Nenhum registro" : primeiroRegistro + "–" + ultimoRegistro + " de " + total, null);
        writer.endElement("span");

        escreverBotaoPaginacao(writer, "proxima", "›", pagina + 1 >= totalPaginas);
        escreverBotaoPaginacao(writer, "ultima", "»", pagina + 1 >= totalPaginas);

        writer.endElement("div");
    }

    private void escreverBotaoPaginacao(ResponseWriter writer, String acao, String rotulo, boolean desabilitado)
            throws IOException {
        writer.startElement("button", this);
        writer.writeAttribute("type", "button", null);
        writer.writeAttribute("class", "box-datatable-paginacao-botao", null);
        writer.writeAttribute("data-acao", acao, null);
        if (desabilitado) {
            writer.writeAttribute("disabled", "disabled", null);
        }
        writer.writeText(rotulo, null);
        writer.endElement("button");
    }

    private String renderPara(String nomeEvento, String clientId) {
        List<ClientBehavior> comportamentosDoEvento = comportamentos.get(nomeEvento);
        if (comportamentosDoEvento != null) {
            for (ClientBehavior comportamento : comportamentosDoEvento) {
                if (comportamento instanceof AjaxBehavior ajax && ajax.getRender() != null
                        && !ajax.getRender().isEmpty()) {
                    return String.join(" ", ajax.getRender());
                }
            }
        }
        return "select".equals(nomeEvento) ? "@none" : clientId;
    }
}
