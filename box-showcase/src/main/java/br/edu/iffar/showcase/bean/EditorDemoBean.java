package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Estado da página de demonstração do b:editor (/editor.xhtml). SessionScoped
 * pelo mesmo motivo do ConfirmDemoBean: sobrevive a reload da página, útil
 * pros testes E2E inspecionarem o valor salvo.
 */
@Named
@SessionScoped
public class EditorDemoBean implements Serializable {

    private String conteudo = "";

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
