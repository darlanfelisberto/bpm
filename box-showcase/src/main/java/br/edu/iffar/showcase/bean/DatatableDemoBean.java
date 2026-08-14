package br.edu.iffar.showcase.bean;

import br.edu.iffar.box.component.datatable.Datatable;
import br.edu.iffar.box.component.datatable.DatatableLazyModel;
import br.edu.iffar.box.component.datatable.DatatablePage;
import br.edu.iffar.box.component.datatable.DatatableQuery;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * State for the b:datatable demo page (/datatable.xhtml).
 * Implements DatatableLazyModel directly on the bean - synthetic in-memory
 * dataset, but load() only returns the requested slice (like a paginated
 * query against a real database would). Doesn't set the "pageSize"
 * attribute on the tag: page size here is controlled by the interface's
 * own defaultPageSize() method.
 */
@Named
@SessionScoped
public class DatatableDemoBean implements Serializable, DatatableLazyModel<Person> {

    private static final String[] FIRST_NAMES = {
            "Alice", "Bob", "Carol", "Diana", "Ethan", "Fiona", "George", "Hannah",
            "Ivy", "Jack", "Karen", "Leo", "Maya", "Nathan", "Olivia", "Peter"
    };
    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Brown", "Taylor", "Miller", "Davis", "Wilson", "Moore"
    };
    private static final String[] ROLES = {
            "Developer", "Analyst", "Coordinator", "Intern", "Manager"
    };

    private List<Person> people;
    private String lastSelection = "";

    @PostConstruct
    void init() {
        populate();
    }

    private void populate() {
        people = new ArrayList<>();
        for (int i = 1; i <= 63; i++) {
            String name = FIRST_NAMES[i % FIRST_NAMES.length] + " " + LAST_NAMES[(i / FIRST_NAMES.length) % LAST_NAMES.length];
            String email = name.toLowerCase(Locale.ROOT).replace(" ", ".") + "@iffar.edu.br";
            people.add(new Person((long) i, name, email, ROLES[i % ROLES.length],
                    LocalDate.of(2016, 1, 1).plusDays(i * 17L)));
        }
    }

    public String getLastSelection() {
        return lastSelection;
    }

    public void onSelect(AjaxBehaviorEvent event) {
        Datatable datatable = (Datatable) event.getComponent();
        Object selected = datatable.getSelectedObject();
        lastSelection = selected != null
                ? "select: " + selected
                : "select: id " + datatable.getRowId() + " not found";
    }

    public void reset() {
        populate();
        lastSelection = "";
    }

    @Override
    public int defaultPageSize() {
        return 8;
    }

    @Override
    public DatatablePage<Person> load(DatatableQuery query) {
        List<Person> filtered = people.stream()
                .filter(person -> matches(person, query.filters()))
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<Person> comparator = comparatorFor(query.sortBy());
        if (comparator != null) {
            filtered.sort(query.sortAscending() ? comparator : comparator.reversed());
        }

        int total = filtered.size();
        if (query.first() >= total) {
            return new DatatablePage<>(List.of(), total);
        }
        int end = Math.min(query.first() + query.size(), total);
        return new DatatablePage<>(new ArrayList<>(filtered.subList(query.first(), end)), total);
    }

    private boolean matches(Person person, Map<String, String> filters) {
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String value = fieldValue(person, filter.getKey());
            if (value == null || !value.toLowerCase(Locale.ROOT).contains(filter.getValue().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    private String fieldValue(Person person, String field) {
        return switch (field) {
            case "name" -> person.getName();
            case "email" -> person.getEmail();
            case "role" -> person.getRole();
            case "hireDate" -> person.getHireDate() != null ? person.getHireDate().toString() : null;
            default -> null;
        };
    }

    private Comparator<Person> comparatorFor(String field) {
        if (field == null) {
            return null;
        }
        return switch (field) {
            case "name" -> Comparator.comparing(Person::getName, Comparator.nullsLast(String::compareTo));
            case "email" -> Comparator.comparing(Person::getEmail, Comparator.nullsLast(String::compareTo));
            case "role" -> Comparator.comparing(Person::getRole, Comparator.nullsLast(String::compareTo));
            case "hireDate" ->
                    Comparator.comparing(Person::getHireDate, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> null;
        };
    }

    /** Used by PersonConverter (injected via CDI) to reconstruct the selected row from its id. */
    Person findById(long id) {
        return people.stream().filter(person -> person.getId() == id).findFirst().orElse(null);
    }
}
