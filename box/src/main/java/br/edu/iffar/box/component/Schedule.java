package br.edu.iffar.box.component;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agenda de eventos (mês/semana/dia), equivalente ao p:schedule do
 * PrimeFaces. Usa o FullCalendar (auto-hospedado em vendor/fullcalendar/,
 * sem CDN) como motor - mesma lib que o próprio p:schedule usa por baixo.
 * Componente nativo (UIComponentBase, não composite) que implementa
 * ClientBehaviorHolder pra suportar f:ajax aninhado nos eventos
 * "select" (arrastou pra selecionar um intervalo vazio), "move" (arrastou
 * um evento existente), "resize" (redimensionou um evento existente) e
 * "click" (clicou num evento existente) - o componente só reporta o que
 * aconteceu (datas/id do evento, via getInicio()/getFim()/getEventoId()
 * no listener), quem decide o que fazer é a página/bean, igual ao
 * b:confirm não decidir o que "confirmar" faz.
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <b:schedule events="#{bean.eventos}">
 *          <f:ajax event="select" listener="#{bean.aoSelecionar}" render=":formNovo"/>
 *          <f:ajax event="move" listener="#{bean.aoMover}" render=":formLista"/>
 *          <f:ajax event="resize" listener="#{bean.aoRedimensionar}" render=":formLista"/>
 *          <f:ajax event="click" listener="#{bean.aoClicar}" render=":formDetalhe"/>
 *      </b:schedule>
 */
@FacesComponent(
        value = Schedule.COMPONENT_TYPE,
        createTag = true,
        tagName = "schedule",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "vendor/fullcalendar/fullcalendar.js", target = "head"),
        @ResourceDependency(library = "box", name = "schedule.css", target = "head"),
        @ResourceDependency(library = "box", name = "box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "schedule.js", target = "head")
})
public class Schedule extends UIComponentBase implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Schedule";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Schedule";

    private static final List<String> EVENT_NAMES =
            Collections.unmodifiableList(List.of("select", "move", "resize", "click"));

    private final Map<String, List<ClientBehavior>> comportamentos = new HashMap<>();

    // Dados do evento decodificado nesta requisição - não fazem parte do
    // estado do componente (getStateHelper), são só o "recado" desta
    // requisição pro listener ler via evento.getComponent().
    private transient String dadoInicio;
    private transient String dadoFim;
    private transient String dadoEventoId;

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
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
    public List<ScheduleEvent> getEvents() {
        return (List<ScheduleEvent>) getStateHelper().eval("events");
    }

    public void setEvents(List<ScheduleEvent> events) {
        getStateHelper().put("events", events);
    }

    /** Data/hora de início do intervalo selecionado ("select") ou do evento movido/redimensionado ("move"/"resize"). */
    public LocalDateTime getInicio() {
        return parseDataHora(dadoInicio);
    }

    /** Data/hora de fim do intervalo selecionado ("select") ou do evento movido/redimensionado ("move"/"resize"). */
    public LocalDateTime getFim() {
        return parseDataHora(dadoFim);
    }

    /** Id do evento movido/redimensionado/clicado - null em "select" (nenhum evento existente envolvido). */
    public String getEventoId() {
        return dadoEventoId;
    }

    // Pacote-privado (nao private) de proposito: permite teste unitario
    // direto (ScheduleParseDataHoraTest, mesmo pacote) sem reflection.
    static LocalDateTime parseDataHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        // O formato que o FullCalendar manda varia com o tipo de interacao:
        // "select" manda data/hora sem offset (ex.: "2026-09-03T00:00");
        // mover/redimensionar um evento manda COM offset de fuso (ex.:
        // "2026-08-14T10:00:00-03:00"); uma selecao/evento de dia inteiro
        // manda so a data (ex.: "2026-08-20"). Tenta os tres formatos.
        try {
            return LocalDateTime.parse(texto);
        } catch (DateTimeParseException semHora) {
            try {
                return OffsetDateTime.parse(texto).toLocalDateTime();
            } catch (DateTimeParseException comOffset) {
                return LocalDate.parse(texto).atStartOfDay();
            }
        }
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> parametros = context.getExternalContext().getRequestParameterMap();

        // So processa se esta requisicao ajax for de fato sobre este
        // componente - o mesmo formulario pode ter outros elementos
        // disparando ajax (ex.: um h:commandButton qualquer na pagina).
        if (!clientId.equals(parametros.get("jakarta.faces.source"))) {
            return;
        }
        String nomeEvento = parametros.get("jakarta.faces.behavior.event");
        if (nomeEvento == null) {
            return;
        }

        dadoInicio = parametros.get(clientId + "_inicio");
        dadoFim = parametros.get(clientId + "_fim");
        dadoEventoId = parametros.get(clientId + "_eventoId");

        List<ClientBehavior> comportamentosDoEvento = comportamentos.get(nomeEvento);
        if (comportamentosDoEvento != null) {
            for (ClientBehavior comportamento : comportamentosDoEvento) {
                comportamento.decode(context, this);
            }
        }
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
        writer.writeAttribute("class", "box-schedule", null);
        for (String nomeEvento : EVENT_NAMES) {
            writer.writeAttribute("data-render-" + nomeEvento, renderPara(nomeEvento), null);
        }

        // Estado inicial dos eventos: script application/json, nao
        // executavel pelo navegador (schedule.js le via JSON.parse do
        // textContent). "<" escapado como < evita que um titulo com
        // "</script>" feche a tag mais cedo e vaze HTML pro resto da
        // pagina - o parser HTML procura essa sequencia literal
        // independente do type do script.
        writer.startElement("script", this);
        writer.writeAttribute("type", "application/json", null);
        writer.writeAttribute("class", "box-schedule-eventos", null);
        writer.write(eventosComoJson().replace("<", "\\u003C"));
        writer.endElement("script");

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-schedule-calendario", null);
        writer.endElement("div");
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div");
    }

    private String eventosComoJson() {
        JsonArrayBuilder array = Json.createArrayBuilder();
        List<ScheduleEvent> lista = getEvents();
        if (lista != null) {
            for (ScheduleEvent evento : lista) {
                JsonObjectBuilder objeto = Json.createObjectBuilder();
                if (evento.getId() != null) {
                    objeto.add("id", evento.getId());
                }
                objeto.add("title", evento.getTitulo() != null ? evento.getTitulo() : "");
                if (evento.getInicio() != null) {
                    objeto.add("start", evento.getInicio().toString());
                }
                if (evento.getFim() != null) {
                    objeto.add("end", evento.getFim().toString());
                }
                objeto.add("allDay", evento.isDiaTodo());
                if (evento.getCor() != null) {
                    objeto.add("color", evento.getCor());
                }
                array.add(objeto);
            }
        }
        return array.build().toString();
    }

    // Le o "render" configurado no <f:ajax event="..." render="..."/>
    // aninhado, pra passar pro schedule.js chamar faces.ajax.request() com
    // o mesmo alvo que o desenvolvedor declarou - sem isso o componente
    // teria que reinventar um jeito proprio de configurar render.
    private String renderPara(String nomeEvento) {
        List<ClientBehavior> comportamentosDoEvento = comportamentos.get(nomeEvento);
        if (comportamentosDoEvento != null) {
            for (ClientBehavior comportamento : comportamentosDoEvento) {
                if (comportamento instanceof AjaxBehavior ajax && ajax.getRender() != null
                        && !ajax.getRender().isEmpty()) {
                    return String.join(" ", ajax.getRender());
                }
            }
        }
        return "@none";
    }
}
