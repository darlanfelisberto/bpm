package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@SessionScoped
public class ButtonDemoBean implements Serializable {

    private int clickCount = 0;
    private String lastAction = "None";

    public void increment() {
        this.clickCount++;
        this.lastAction = "Incremented counter to " + clickCount;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Clicked", "Counter is now " + clickCount));
    }

    public void reset() {
        this.clickCount = 0;
        this.lastAction = "Counter reset to 0";
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Reset", "Counter has been reset to 0"));
    }

    public void execute(String actionName) {
        this.lastAction = "Executed: " + actionName;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Action executed", actionName));
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }
}
