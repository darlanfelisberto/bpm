package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Estado da página de demonstração do b:growl (/growl.xhtml): cada botão
 * adiciona uma FacesMessage de severidade diferente, que o b:growl no
 * "render" do próprio botão exibe como toast.
 */
@Named
@SessionScoped
public class GrowlDemoBean implements Serializable {

    public void avisarInfo() {
        adicionar(FacesMessage.SEVERITY_INFO, "Salvo com sucesso", "O registro foi gravado.");
    }

    public void avisarAlerta() {
        adicionar(FacesMessage.SEVERITY_WARN, "Atenção", "Alguns campos opcionais ficaram em branco.");
    }

    public void avisarErro() {
        adicionar(FacesMessage.SEVERITY_ERROR, "Falha ao salvar", "Verifique os campos destacados.");
    }

    public void avisarFixo() {
        adicionar(FacesMessage.SEVERITY_WARN, "Sessão expirando", "Fica na tela até você fechar.");
    }

    private void adicionar(FacesMessage.Severity severidade, String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, resumo, detalhe));
    }
}
