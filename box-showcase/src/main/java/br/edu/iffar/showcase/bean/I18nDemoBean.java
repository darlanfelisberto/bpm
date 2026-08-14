package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalTime;

/** State for the i18n demo page (/i18n.xhtml) - just reports that the confirmed action ran. */
@Named
@SessionScoped
public class I18nDemoBean implements Serializable {

    private String lastAction = "";

    public String getLastAction() {
        return lastAction;
    }

    public void confirm() {
        lastAction = "Confirmed at " + LocalTime.now().withNano(0);
    }
}
