package br.edu.iffar.box.component.autocomplete;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocompleteTest {

    @Test
    void limitResultsWithNullReturnsEmptyList() {
        assertEquals(Collections.emptyList(), Autocomplete.limitResults(null, 5));
    }

    @Test
    void limitResultsWithNullOrZeroMaxResultsReturnsOriginalList() {
        List<String> items = List.of("A", "B", "C");
        assertEquals(items, Autocomplete.limitResults(items, null));
        assertEquals(items, Autocomplete.limitResults(items, 0));
        assertEquals(items, Autocomplete.limitResults(items, -1));
    }

    @Test
    void limitResultsWithMaxResultsLargerThanSizeReturnsAll() {
        List<String> items = List.of("A", "B", "C");
        assertEquals(items, Autocomplete.limitResults(items, 10));
    }

    @Test
    void limitResultsTruncatesToMaxResults() {
        List<String> items = List.of("A", "B", "C", "D", "E");
        List<?> limited = Autocomplete.limitResults(items, 3);
        assertEquals(3, limited.size());
        assertEquals(List.of("A", "B", "C"), limited);
    }
}
