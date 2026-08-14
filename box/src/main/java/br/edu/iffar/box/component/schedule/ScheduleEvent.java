package br.edu.iffar.box.component.schedule;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * An event displayed by b:schedule. Simple POJO - whoever uses the
 * component manages its own list (create/move/resize arrive via the
 * "select"/"move"/"resize" client behaviors; this object only carries
 * what already exists, to draw the calendar).
 */
public class ScheduleEvent implements Serializable {

    private String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean allDay;
    private String color;

    public ScheduleEvent() {
    }

    public ScheduleEvent(String id, String title, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
