package br.edu.iffar.showcase.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * State for the entity converter demo page (/entity-converter.xhtml).
 */
@Named
@ViewScoped
public class EntityConverterDemoBean implements Serializable {

    private List<Person> people;
    private Person selectedPerson;
    private String message;

    @PostConstruct
    void init() {
        people = List.of(
                new Person(1L, "Alice Smith", "alice.smith@iffar.edu.br", "Developer", LocalDate.of(2021, 3, 15)),
                new Person(2L, "Bob Johnson", "bob.johnson@iffar.edu.br", "Analyst", LocalDate.of(2020, 7, 1)),
                new Person(3L, "Carol Brown", "carol.brown@iffar.edu.br", "Coordinator", LocalDate.of(2019, 11, 20)),
                new Person(4L, "Diana Taylor", "diana.taylor@iffar.edu.br", "Manager", LocalDate.of(2018, 5, 10))
        );
    }

    public List<Person> getPeople() {
        return people;
    }

    public Person getSelectedPerson() {
        return selectedPerson;
    }

    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    public String getMessage() {
        return message;
    }

    public void submit() {
        if (selectedPerson != null) {
            message = "Converted successfully: " + selectedPerson.getName() + " (ID: " + selectedPerson.getId() + ")";
        } else {
            message = "No person selected.";
        }
    }
}
