package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * State for the b:confirm demo page (/confirm.xhtml): two independent
 * lists, one for each way of using the confirmation (nested b:confirm
 * behavior, and the data-box-confirm attribute without a Faces component).
 * SessionScoped (not ViewScoped) to survive "mvn liberty:dev" reloading the
 * page and to keep it simple to restore via reload during E2E tests.
 */
@Named
@SessionScoped
public class ConfirmDemoBean implements Serializable {

    private List<String> itemsBehavior = new ArrayList<>(List.of("Item A", "Item B", "Item C"));
    private List<String> itemsAttribute = new ArrayList<>(List.of("Item X", "Item Y", "Item Z"));

    public List<String> getItemsBehavior() {
        return itemsBehavior;
    }

    public List<String> getItemsAttribute() {
        return itemsAttribute;
    }

    public void deleteBehavior(String item) {
        itemsBehavior.remove(item);
    }

    public void deleteAttribute(String item) {
        itemsAttribute.remove(item);
    }

    public void restore() {
        itemsBehavior = new ArrayList<>(List.of("Item A", "Item B", "Item C"));
        itemsAttribute = new ArrayList<>(List.of("Item X", "Item Y", "Item Z"));
    }
}
