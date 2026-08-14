package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * State for the b:growl demo page (/growl.xhtml): each button adds a
 * FacesMessage with a different severity, which the b:growl in the
 * button's own "render" displays as a toast.
 */
@Named
@SessionScoped
public class GrowlDemoBean implements Serializable {

    public void notifyInfo() {
        add(FacesMessage.SEVERITY_INFO, "Saved successfully", "The record has been saved.");
    }

    public void notifyWarning() {
        add(FacesMessage.SEVERITY_WARN, "Warning", "Some optional fields were left blank.");
    }

    public void notifyError() {
        add(FacesMessage.SEVERITY_ERROR, "Failed to save", "Please check the highlighted fields.");
    }

    public void notifyFixed() {
        add(FacesMessage.SEVERITY_WARN, "Session expiring", "Stays on screen until you close it.");
    }

    private void add(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}
