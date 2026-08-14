package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * State of the b:editor demo page (/editor.xhtml). SessionScoped for the
 * same reason as ConfirmDemoBean: survives a page reload, useful for the
 * E2E tests to inspect the saved value.
 */
@Named
@SessionScoped
public class EditorDemoBean implements Serializable {

    private String content = "";

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
