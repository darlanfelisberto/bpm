package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * State for the b:autocomplete demo page (/autocomplete.xhtml).
 */
@Named
@SessionScoped
public class AutocompleteDemoBean implements Serializable {

    private static final List<String> CITIES = List.of(
            "Alegrete", "Frederico Westphalen", "Jaguari", "Júlio de Castilhos",
            "Panambi", "Porto Alegre", "Santa Maria", "Santo Ângelo",
            "Santo Augusto", "São Borja", "São Vicente do Sul", "Uruguaiana"
    );

    @Inject
    private DatatableDemoBean datatableDemoBean;

    private String selectedCity;
    private Person selectedPerson;
    private Person selectedPersonCustom;
    private String lastSelection = "";

    public List<String> completeCity(String query) {
        String filter = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        if (filter.isEmpty()) {
            return CITIES;
        }
        return CITIES.stream()
                .filter(city -> city.toLowerCase(Locale.ROOT).contains(filter))
                .toList();
    }

    public List<Person> completePerson(String query) {
        String filter = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        List<Person> all = datatableDemoBean.load(new br.edu.iffar.box.component.datatable.DatatableQuery(0, 100, "name", true, java.util.Map.of())).rows();
        if (filter.isEmpty()) {
            return all.stream().limit(10).toList();
        }
        return all.stream()
                .filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(filter)
                        || p.getEmail().toLowerCase(Locale.ROOT).contains(filter)
                        || p.getRole().toLowerCase(Locale.ROOT).contains(filter))
                .limit(10)
                .toList();
    }

    public void onPersonSelect(AjaxBehaviorEvent event) {
        lastSelection = selectedPerson != null ? "Selected person: " + selectedPerson : "No person selected";
    }

    public void onPersonCustomSelect(AjaxBehaviorEvent event) {
        lastSelection = selectedPersonCustom != null ? "Selected person (custom): " + selectedPersonCustom : "No person selected";
    }

    public void onCitySelect(AjaxBehaviorEvent event) {
        lastSelection = selectedCity != null ? "Selected city: " + selectedCity : "No city selected";
    }

    public void reset() {
        selectedCity = null;
        selectedPerson = null;
        selectedPersonCustom = null;
        lastSelection = "";
    }

    public String getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(String selectedCity) {
        this.selectedCity = selectedCity;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    public Person getSelectedPersonCustom() {
        return selectedPersonCustom;
    }

    public void setSelectedPersonCustom(Person selectedPersonCustom) {
        this.selectedPersonCustom = selectedPersonCustom;
    }

    public String getLastSelection() {
        return lastSelection;
    }

    public void setLastSelection(String lastSelection) {
        this.lastSelection = lastSelection;
    }
}
