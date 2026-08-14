package br.edu.iffar.box.component.datatable;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Datatable's pure logic (parsing/computation, no FacesContext): the
 * sort cycle, parsing the filter JSON coming from the request, and the
 * page clamp when a filter reduces the total below the current page.
 */
class DatatableTest {

    @Test
    void clickingNewFieldAlwaysGoesToAscending() {
        assertEquals(new Datatable.Sort("name", true), Datatable.nextSort("name", null, true));
        assertEquals(new Datatable.Sort("name", true), Datatable.nextSort("name", "email", false));
    }

    @Test
    void clickingSameFieldAgainCyclesAscDescNone() {
        // none -> asc (field was not the sorted one yet)
        assertEquals(new Datatable.Sort("name", true), Datatable.nextSort("name", null, true));
        // asc -> desc
        assertEquals(new Datatable.Sort("name", false), Datatable.nextSort("name", "name", true));
        // desc -> none (field null - Datatable.currentSortBy() falls back to the default, if any)
        Datatable.Sort backToNone = Datatable.nextSort("name", "name", false);
        assertNull(backToNone.field());
        assertTrue(backToNone.ascending());
    }

    @Test
    void parseFiltersNullOrBlankValueBecomesEmptyMap() {
        assertEquals(Map.of(), Datatable.parseFilters(null));
        assertEquals(Map.of(), Datatable.parseFilters(""));
        assertEquals(Map.of(), Datatable.parseFilters("   "));
    }

    @Test
    void parseFiltersValidJson() {
        assertEquals(Map.of("name", "ana", "role", "dev"),
                Datatable.parseFilters("{\"name\":\"ana\",\"role\":\"dev\"}"));
    }

    @Test
    void parseFiltersIgnoresBlankFields() {
        // blank field = "no filter for this field", not "filter by empty"
        assertEquals(Map.of("name", "ana"), Datatable.parseFilters("{\"name\":\"ana\",\"role\":\"\",\"email\":\"  \"}"));
    }

    @Test
    void parseFiltersTamperedOrMalformedJsonBecomesEmptyMap() {
        assertEquals(Map.of(), Datatable.parseFilters("not json"));
        assertEquals(Map.of(), Datatable.parseFilters("[1,2,3]"));
        assertEquals(Map.of(), Datatable.parseFilters("{\"name\": 42}"));
    }

    @Test
    void parseIntNullBlankOrInvalidValueBecomesNull() {
        assertNull(Datatable.parseInt(null));
        assertNull(Datatable.parseInt(""));
        assertNull(Datatable.parseInt("abc"));
    }

    @Test
    void parseIntValidValue() {
        assertEquals(3, Datatable.parseInt("3"));
    }

    @Test
    void clampPageToBoundsWithinLimitDoesNotChange() {
        assertEquals(2, Datatable.clampPageToBounds(2, 10, 25));
    }

    @Test
    void clampPageToBoundsOutOfLimitGoesToLastValidPage() {
        // 25 records, 10 per page -> pages 0,1,2 (25 lands on page 2: 20-24)
        assertEquals(2, Datatable.clampPageToBounds(5, 10, 25));
        // total exactly a multiple of page size - last page is the previous one
        assertEquals(1, Datatable.clampPageToBounds(4, 10, 20));
    }

    @Test
    void clampPageToBoundsWithNoRecordsGoesToZero() {
        assertEquals(0, Datatable.clampPageToBounds(3, 10, 0));
    }
}
