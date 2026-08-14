package br.edu.iffar.showcase.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers /schedule.xhtml (b:schedule): clicking an existing event ("click"),
 * and dragging an event to another day ("move") - the second case is the
 * one that caught a real bug (see the fix commit): FullCalendar sends the
 * new date with a timezone offset (e.g. "2026-08-16T10:00:00-03:00") when
 * moving an event, a format that neither LocalDateTime.parse() nor
 * LocalDate.parse() accept on their own - it only showed up when testing an
 * event with a real time being dragged, not with the plain "select"
 * selection.
 */
class ScheduleIT extends PlaywrightSupport {

    @Test
    void clickingEventReportsItsId() {
        page.navigate(BASE_URL + "/schedule.xhtml");

        Locator event = page.locator(".box-schedule-calendar").getByText("Team meeting").first();
        event.waitFor();

        // #last-action is already in the DOM since the initial load - only
        // the content changes via ajax. A waitFor() on it would return
        // right away, without actually waiting for the ajax to finish
        // (same gotcha documented in EditorIT). Wait for the POST response
        // instead.
        page.waitForResponse(response -> response.url().contains("/schedule.xhtml") && "POST".equals(response.request().method()),
                event::click);

        Locator result = page.locator("#last-action");
        assertTrue(result.textContent().startsWith("click: evento"),
                "clicking an event should report \"click: evento <id>\", but it was: " + result.textContent());
    }

    @Test
    void draggingEventToAnotherDayReportsNewDates() {
        page.navigate(BASE_URL + "/schedule.xhtml");
        // Week view: bigger event blocks, easier to drag precisely than in
        // month view (where the event is just a thin one-line pill).
        page.locator(".fc-timeGridWeek-button").click();

        Locator event = page.locator(".box-schedule-calendar").getByText("Team meeting").first();
        event.waitFor();
        // Without this the bounding box comes relative to the whole page
        // (not the visible viewport) - the event ends up well below the
        // fold (the page has a lot of documentation above the calendar),
        // and mouse clicks at "off-screen" coordinates simply don't happen.
        event.scrollIntoViewIfNeeded();
        BoundingBox eventOrigin = event.boundingBox();

        // Find the event's current day column and the next one (the drag
        // target) by measuring the actual columns, instead of guessing a
        // fixed pixel offset - more reliable than just adding a "typical"
        // column width value.
        Locator columns = page.locator(".fc-timegrid-col[data-date]");
        int totalColumns = columns.count();
        double eventCenterX = eventOrigin.x + eventOrigin.width / 2;
        int currentIndex = -1;
        BoundingBox[] boxes = new BoundingBox[totalColumns];
        for (int i = 0; i < totalColumns; i++) {
            boxes[i] = columns.nth(i).boundingBox();
            if (eventCenterX >= boxes[i].x && eventCenterX <= boxes[i].x + boxes[i].width) {
                currentIndex = i;
            }
        }
        BoundingBox target = boxes[currentIndex + 1];
        double targetX = target.x + target.width / 2;
        double targetY = eventOrigin.y + eventOrigin.height / 2;

        page.mouse().move(eventCenterX, eventOrigin.y + eventOrigin.height / 2);
        page.mouse().down();
        page.mouse().move(targetX, targetY, new Mouse.MoveOptions().setSteps(5));
        page.mouse().up();

        Locator result = page.locator("#last-action");
        result.getByText("move:").waitFor();
        String text = result.textContent();
        assertTrue(text.startsWith("move: event"),
                "dragging an event should report \"move: event <id> to <start> until <end>\", but it was: " + text);
        assertTrue(text.contains("10:00") && text.contains("11:00"),
                "moving to another day shouldn't change the time (10:00-11:00), but it was: " + text);
    }
}
