package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Backing bean for demonstrating server actions and ajax from b:menuitem.
 */
@Named
@RequestScoped
public class MenuDemoBean {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String lastAction;

    public String getLastAction() {
        return lastAction;
    }

    public void executeAction(String actionName) {
        this.lastAction = String.format("Executed '%s' at %s", actionName, LocalTime.now().format(TIME_FORMAT));
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Action executed", this.lastAction));
    }
}
