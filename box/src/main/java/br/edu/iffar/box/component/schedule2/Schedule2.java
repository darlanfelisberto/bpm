package br.edu.iffar.box.component.schedule2;

import br.edu.iffar.box.component.schedule.ScheduleEvent;

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
 * Same as b:schedule, but with no external lib at all: just a month grid
 * (no week/day view, no resizing - whole-day granularity can't be
 * "stretched" without an hour grid, which is precisely the most
 * laborious part to reproduce by hand) drawn in plain vanilla JS
 * (schedule2.js), no FullCalendar. Exists to compare the result - see
 * box-showcase/schedule2.xhtml - not to replace b:schedule.
 *
 * Same client behaviors "select" (click an empty day) and "click"
 * (click an event), plus "move" (drag an event to another day, via
 * native HTML5 Drag and Drop - draggable="true"/dragstart/dragover/drop,
 * no lib at all).
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <b:schedule2 events="#{bean.events}">
 *          <f:ajax event="select" listener="#{bean.onSelect}" render=":result"/>
 *          <f:ajax event="move" listener="#{bean.onMove}" render=":result"/>
 *          <f:ajax event="click" listener="#{bean.onClick}" render=":result"/>
 *      </b:schedule2>
 */
@FacesComponent(
        value = Schedule2.COMPONENT_TYPE,
        createTag = true,
        tagName = "schedule2",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "box.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "schedule2/schedule2.js", target = "head")
})
public class Schedule2 extends UIComponentBase implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Schedule2";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Schedule2";

    private static final List<String> EVENT_NAMES =
            Collections.unmodifiableList(List.of("select", "move", "click"));

    private final Map<String, List<ClientBehavior>> behaviors = new HashMap<>();

    private transient String startData;
    private transient String endData;
    private transient String eventIdData;

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public Map<String, List<ClientBehavior>> getClientBehaviors() {
        return Collections.unmodifiableMap(behaviors);
    }

    @Override
    public void addClientBehavior(String eventName, ClientBehavior behavior) {
        behaviors.computeIfAbsent(eventName, k -> new ArrayList<>()).add(behavior);
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

    /** Start date of the selected day ("select") or new date of the moved event ("move"). */
    public LocalDateTime getStart() {
        return parseDateTime(startData);
    }

    /** End date of the selected day ("select") or new end date of the moved event ("move"). */
    public LocalDateTime getEnd() {
        return parseDateTime(endData);
    }

    /** Id of the moved/clicked event - null on "select". */
    public String getEventId() {
        return eventIdData;
    }

    static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException noTime) {
            try {
                return OffsetDateTime.parse(text).toLocalDateTime();
            } catch (DateTimeParseException withOffset) {
                return LocalDate.parse(text).atStartOfDay();
            }
        }
    }

    @Override
    public void decode(FacesContext context) {
        if (!isRendered()) {
            return;
        }
        String clientId = getClientId(context);
        Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();

        if (!clientId.equals(parameters.get("jakarta.faces.source"))) {
            return;
        }
        String eventName = parameters.get("jakarta.faces.behavior.event");
        if (eventName == null) {
            return;
        }

        startData = parameters.get(clientId + "_start");
        endData = parameters.get(clientId + "_end");
        eventIdData = parameters.get(clientId + "_eventId");

        List<ClientBehavior> behaviorsForEvent = behaviors.get(eventName);
        if (behaviorsForEvent != null) {
            for (ClientBehavior behavior : behaviorsForEvent) {
                behavior.decode(context, this);
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
        for (String eventName : EVENT_NAMES) {
            writer.writeAttribute("data-render-" + eventName, renderTargetFor(eventName), null);
        }

        // Same escaping care as b:schedule: "<" becomes < so a title
        // with "</script>" doesn't close the tag early.
        writer.startElement("script", this);
        writer.writeAttribute("type", "application/json", null);
        writer.writeAttribute("class", "box-schedule2-events", null);
        writer.write(eventsAsJson().replace("<", "\\u003C"));
        writer.endElement("script");

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-schedule2-calendar", null);
        writer.endElement("div");
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        context.getResponseWriter().endElement("div");
    }

    private String eventsAsJson() {
        JsonArrayBuilder array = Json.createArrayBuilder();
        List<ScheduleEvent> events = getEvents();
        if (events != null) {
            for (ScheduleEvent event : events) {
                JsonObjectBuilder object = Json.createObjectBuilder();
                if (event.getId() != null) {
                    object.add("id", event.getId());
                }
                object.add("title", event.getTitle() != null ? event.getTitle() : "");
                if (event.getStart() != null) {
                    object.add("start", event.getStart().toString());
                }
                if (event.getEnd() != null) {
                    object.add("end", event.getEnd().toString());
                }
                if (event.getColor() != null) {
                    object.add("color", event.getColor());
                }
                array.add(object);
            }
        }
        return array.build().toString();
    }

    private String renderTargetFor(String eventName) {
        List<ClientBehavior> behaviorsForEvent = behaviors.get(eventName);
        if (behaviorsForEvent != null) {
            for (ClientBehavior behavior : behaviorsForEvent) {
                if (behavior instanceof AjaxBehavior ajax && ajax.getRender() != null
                        && !ajax.getRender().isEmpty()) {
                    return String.join(" ", ajax.getRender());
                }
            }
        }
        return "@none";
    }
}
