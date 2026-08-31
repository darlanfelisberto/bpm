package br.edu.iffar.box.component.schedule;

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
 * Event schedule (month/week/day), equivalent to PrimeFaces' p:schedule.
 * Uses FullCalendar (self-hosted under vendor/fullcalendar/, no CDN) as
 * the engine - the same lib p:schedule itself uses under the hood.
 * Native component (UIComponentBase, not composite) that implements
 * ClientBehaviorHolder to support nested f:ajax on the "select" (dragged
 * to select an empty range), "move" (dragged an existing event),
 * "resize" (resized an existing event) and "click" (clicked an existing
 * event) events - the component only reports what happened (dates/event
 * id, via getStart()/getEnd()/getEventId() in the listener), it's up to
 * the page/bean to decide what to do, just like box-confirm doesn't decide
 * what "confirm" does.
 *
 * Usage: xmlns:b="http://iffar.edu.br/box"
 *      <b:schedule events="#{bean.events}">
 *          <f:ajax event="select" listener="#{bean.onSelect}" render=":newForm"/>
 *          <f:ajax event="move" listener="#{bean.onMove}" render=":listForm"/>
 *          <f:ajax event="resize" listener="#{bean.onResize}" render=":listForm"/>
 *          <f:ajax event="click" listener="#{bean.onClick}" render=":detailForm"/>
 *      </b:schedule>
 */
@FacesComponent(
        value = Schedule.COMPONENT_TYPE,
        createTag = true,
        tagName = "schedule",
        namespace = "http://iffar.edu.br/box")
@ResourceDependencies({
        @ResourceDependency(library = "box", name = "vendor/fullcalendar/fullcalendar.js", target = "head"),
        @ResourceDependency(library = "box", name = "box.css", target = "head"),
        @ResourceDependency(library = "box", name = "core/box-core.js", target = "head"),
        @ResourceDependency(library = "box", name = "schedule/schedule.js", target = "head")
})
public class Schedule extends UIComponentBase implements ClientBehaviorHolder {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Schedule";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Schedule";

    private static final List<String> EVENT_NAMES =
            Collections.unmodifiableList(List.of("select", "move", "resize", "click"));

    private final Map<String, List<ClientBehavior>> behaviors = new HashMap<>();

    // Decoded event data for this request - not part of the component's
    // state (getStateHelper), just the "message" for this request for
    // the listener to read via event.getComponent().
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

    /** Start date/time of the selected range ("select") or of the moved/resized event ("move"/"resize"). */
    public LocalDateTime getStart() {
        return parseDateTime(startData);
    }

    /** End date/time of the selected range ("select") or of the moved/resized event ("move"/"resize"). */
    public LocalDateTime getEnd() {
        return parseDateTime(endData);
    }

    /** Id of the moved/resized/clicked event - null on "select" (no existing event involved). */
    public String getEventId() {
        return eventIdData;
    }

    // Package-private (not private) on purpose: allows the unit test
    // (ScheduleParseDateTimeTest, same package) to call it directly
    // without reflection.
    static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        // The format FullCalendar sends varies with the kind of
        // interaction: "select" sends date/time without offset (e.g.
        // "2026-09-03T00:00"); moving/resizing an event sends it WITH a
        // timezone offset (e.g. "2026-08-14T10:00:00-03:00"); a whole-day
        // selection/event sends just the date (e.g. "2026-08-20"). Tries
        // all three formats.
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

        // Only processes if this ajax request is actually about this
        // component - the same form may have other elements triggering
        // ajax (e.g. some unrelated h:commandButton on the page).
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
        writer.writeAttribute("class", "box-schedule", null);
        for (String eventName : EVENT_NAMES) {
            writer.writeAttribute("data-render-" + eventName, renderTargetFor(eventName), null);
        }

        // Initial event state: application/json script, not executable
        // by the browser (schedule.js reads it via JSON.parse on
        // textContent). "<" is escaped as < so a title containing
        // "</script>" can't close the tag early and leak HTML into the
        // rest of the page - the HTML parser looks for that literal
        // sequence regardless of the script's type.
        writer.startElement("script", this);
        writer.writeAttribute("type", "application/json", null);
        writer.writeAttribute("class", "box-schedule-events", null);
        writer.write(eventsAsJson().replace("<", "\\u003C"));
        writer.endElement("script");

        writer.startElement("div", this);
        writer.writeAttribute("class", "box-schedule-calendar", null);
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
                object.add("allDay", event.isAllDay());
                if (event.getColor() != null) {
                    object.add("color", event.getColor());
                }
                array.add(object);
            }
        }
        return array.build().toString();
    }

    // Reads the "render" configured on the nested
    // <f:ajax event="..." render="..."/>, to pass along so schedule.js
    // can call faces.ajax.request() with the same target the developer
    // declared - without this the component would have to reinvent its
    // own way to configure render.
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
