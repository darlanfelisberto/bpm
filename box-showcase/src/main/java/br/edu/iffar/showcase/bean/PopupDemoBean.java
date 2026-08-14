package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * State for the b:popup demo page (/popup.xhtml): the popup itself is
 * 100% client-side (Box.popup.open/close) - no "open"/"visible" property
 * here. "save" just shows how the bean tells the client to close the
 * popup after an action, via
 * PartialViewContext.getEvalScripts() (the native Jakarta Faces channel
 * for running arbitrary JS alongside the ajax response), equivalent to
 * PrimeFaces' RequestContext.execute().
 */
@Named
@SessionScoped
public class PopupDemoBean implements Serializable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String lastSaved;

    public String getLastSaved() {
        return lastSaved;
    }

    public void save() {
        lastSaved = "Saved at " + LocalTime.now().format(TIME_FORMAT);
        FacesContext.getCurrentInstance().getPartialViewContext()
                .getEvalScripts().add("Box.popup.close('formPopupServidor:dlgServidor')");
    }
}
