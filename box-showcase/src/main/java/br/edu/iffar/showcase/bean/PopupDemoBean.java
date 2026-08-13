package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Estado da página de demonstração do b:popup (/popup.xhtml): o popup em si
 * é 100% client-side (Box.popup.abrir/fechar) - nenhuma propriedade
 * "aberto"/"visible" aqui. "salvar" só mostra como o bean manda o cliente
 * fechar o popup depois de uma ação, via
 * PartialViewContext.getScriptsToExecute() (o canal nativo do Jakarta
 * Faces pra executar JS arbitrário junto da resposta ajax).
 */
@Named
@SessionScoped
public class PopupDemoBean implements Serializable {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String ultimoSalvamento;

    public String getUltimoSalvamento() {
        return ultimoSalvamento;
    }

    public void salvar() {
        ultimoSalvamento = "Salvo às " + LocalTime.now().format(FORMATO_HORA);
        FacesContext.getCurrentInstance().getPartialViewContext()
                .getEvalScripts().add("Box.popup.fechar('formPopupServidor:dlgServidor')");
    }
}
