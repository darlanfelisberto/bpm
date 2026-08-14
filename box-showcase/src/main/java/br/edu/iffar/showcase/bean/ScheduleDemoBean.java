package br.edu.iffar.showcase.bean;

import br.edu.iffar.box.component.schedule.Schedule;
import br.edu.iffar.box.component.schedule.ScheduleEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * State for the b:schedule demo page (/schedule.xhtml).
 * SessionScoped for the same reason as the other demo beans: survives a
 * page reload, useful for the E2E tests to inspect the state.
 */
@Named
@SessionScoped
public class ScheduleDemoBean implements Serializable {

    private List<ScheduleEvent> events;
    private String lastAction = "";

    @PostConstruct
    void init() {
        LocalDate today = LocalDate.now();
        events = new ArrayList<>();
        events.add(new ScheduleEvent("1", "Team meeting", today.atTime(10, 0), today.atTime(11, 0)));
        events.add(new ScheduleEvent("2", "Report submission", today.plusDays(2).atTime(14, 0), today.plusDays(2).atTime(15, 0)));
    }

    public List<ScheduleEvent> getEvents() {
        return events;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void onSelect(AjaxBehaviorEvent event) {
        Schedule schedule = (Schedule) event.getComponent();
        LocalDateTime start = schedule.getStart();
        LocalDateTime end = schedule.getEnd();
        String id = String.valueOf(System.currentTimeMillis());
        events.add(new ScheduleEvent(id, "New event", start, end));
        lastAction = "select: " + start + " to " + end;
    }

    public void onMove(AjaxBehaviorEvent event) {
        Schedule schedule = (Schedule) event.getComponent();
        updateEvent(schedule.getEventId(), schedule.getStart(), schedule.getEnd());
        lastAction = "move: event " + schedule.getEventId() + " to " + schedule.getStart() + " until " + schedule.getEnd();
    }

    public void onResize(AjaxBehaviorEvent event) {
        Schedule schedule = (Schedule) event.getComponent();
        updateEvent(schedule.getEventId(), schedule.getStart(), schedule.getEnd());
        lastAction = "resize: event " + schedule.getEventId() + " until " + schedule.getEnd();
    }

    public void onClick(AjaxBehaviorEvent event) {
        Schedule schedule = (Schedule) event.getComponent();
        lastAction = "click: evento " + schedule.getEventId();
    }

    private void updateEvent(String id, LocalDateTime start, LocalDateTime end) {
        for (ScheduleEvent event : events) {
            if (event.getId().equals(id)) {
                event.setStart(start);
                event.setEnd(end);
                return;
            }
        }
    }
}
