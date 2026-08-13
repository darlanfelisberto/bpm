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
 * Igual ao b:schedule, mas sem nenhuma lib externa: só grade de mês
 * (sem semana/dia, sem redimensionar - granularidade de dia inteiro não
 * dá pra "esticar" sem uma grade de horas, que é justamente a parte mais
 * trabalhosa de reproduzir na mão) desenhada em JS vanilla puro
 * (schedule2.js), sem FullCalendar. Existe pra comparar o resultado -
 * ver box-showcase/schedule2.xhtml - não pra substituir o b:schedule.
 *
 * Mesmos client behaviors "select" (clicar num dia vazio) e "click"
 * (clicar num evento), mais "move" (arrastar um evento pra outro dia,
 * via Drag and Drop nativo do HTML5 - draggable="true"/dragstart/
 * dragover/drop, sem lib nenhuma).
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <b:schedule2 events="#{bean.eventos}">
 *          <f:ajax event="select" listener="#{bean.aoSelecionar}" render=":resultado"/>
 *          <f:ajax event="move" listener="#{bean.aoMover}" render=":resultado"/>
 *          <f:ajax event="click" listener="#{bean.aoClicar}" render=":resultado"/>
 *      </b:schedule2>
 */
@FacesComponent(
        value = Schedule2.COMPONENT_TYPE,
        createTag = true,
        tagName = "schedule2",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "schedule2.css", target = "head"),
        @ResourceDependency(library = "box", name = "box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "schedule2.js", target = "head")
})
public class Schedule2 extends UIComponentBase implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Schedule2";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Schedule2";

    private static final List<String> EVENT_NAMES =
            Collections.unmodifiableList(List.of("select", "move", "click"));

    private final Map<String, List<ClientBehavior>> comportamentos = new HashMap<>();

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

    /** Data de início do dia selecionado ("select") ou nova data do evento movido ("move"). */
    public LocalDateTime getInicio() {
        return parseDataHora(dadoInicio);
    }

    /** Data de fim do dia selecionado ("select") ou nova data de fim do evento movido ("move"). */
    public LocalDateTime getFim() {
        return parseDataHora(dadoFim);
    }

    /** Id do evento movido/clicado - null em "select". */
    public String getEventoId() {
        return dadoEventoId;
    }

    static LocalDateTime parseDataHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
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
        writer.writeAttribute("class", "box-schedule2", null);
        for (String nomeEvento : EVENT_NAMES) {
            writer.writeAttribute("data-render-" + nomeEvento, renderPara(nomeEvento), null);
        }

        // Mesmo cuidado de escaping do b:schedule: "<" vira < pra um
        // titulo com "</script>" nao fechar a tag mais cedo.
        writer.startElement("script", this);
        writer.writeAttribute("type", "application/json", null);
        writer.writeAttribute("class", "box-schedule2-eventos", null);
        writer.write(eventosComoJson().replace("<", "\\u003C"));
        writer.endElement("script");

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-schedule2-calendario", null);
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
                if (evento.getCor() != null) {
                    objeto.add("color", evento.getCor());
                }
                array.add(objeto);
            }
        }
        return array.build().toString();
    }

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
