package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * State for the box-confirm demo page (/confirm.xhtml): confirmation is
 * triggered by a &lt;box-confirm&gt; custom element nested inside the
 * h:commandLink, no Faces component involved. SessionScoped (not
 * ViewScoped) to survive "mvn liberty:dev" reloading the page and to keep
 * it simple to restore via reload during E2E tests.
 */
@Named
@SessionScoped
public class ConfirmDemoBean implements Serializable {

    private List<String> items = new ArrayList<>(List.of("Item A", "Item B", "Item C"));

    public List<String> getItems() {
        return items;
    }

    public void delete(String item) {
        items.remove(item);
    }

    public void restore() {
        items = new ArrayList<>(List.of("Item A", "Item B", "Item C"));
    }
}
