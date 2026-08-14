package br.edu.iffar.box.component.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests Schedule.parseDateTime() against the three formats FullCalendar
 * actually sends (discovered by testing manually in the browser): no
 * offset ("select"), with a timezone offset (moving/resizing an event),
 * and just the date (whole-day selection/event).
 */
class ScheduleParseDateTimeTest {

    @Test
    void nullOrBlankValueBecomesNull() {
        assertNull(Schedule.parseDateTime(null));
        assertNull(Schedule.parseDateTime(""));
        assertNull(Schedule.parseDateTime("   "));
    }

    @Test
    void dateTimeWithoutOffset() {
        // format that "select" sends
        assertEquals(LocalDateTime.of(2026, 9, 3, 0, 0), Schedule.parseDateTime("2026-09-03T00:00"));
        assertEquals(LocalDateTime.of(2026, 8, 20, 10, 30), Schedule.parseDateTime("2026-08-20T10:30:00"));
    }

    @Test
    void dateTimeWithTimezoneOffset() {
        // format that moving/resizing an event sends - the local time
        // (not the UTC time) is what matters here, the offset is simply discarded.
        assertEquals(LocalDateTime.of(2026, 8, 14, 10, 0), Schedule.parseDateTime("2026-08-14T10:00:00-03:00"));
        assertEquals(LocalDateTime.of(2026, 8, 14, 11, 0), Schedule.parseDateTime("2026-08-14T11:00:00-03:00"));
    }

    @Test
    void dateOnly_becomesStartOfDay() {
        // whole-day selection/event format
        assertEquals(LocalDateTime.of(2026, 8, 20, 0, 0), Schedule.parseDateTime("2026-08-20"));
    }
}
