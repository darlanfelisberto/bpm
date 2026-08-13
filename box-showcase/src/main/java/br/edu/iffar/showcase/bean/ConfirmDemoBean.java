package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Estado da página de demonstração do b:confirm (/confirm.xhtml): duas
 * listas independentes, uma pra cada forma de usar a confirmação (behavior
 * b:confirm aninhado, e atributo data-box-confirm sem componente Faces).
 * SessionScoped (não ViewScoped) pra sobreviver ao "mvn liberty:dev"
 * recarregando a página e pra ficar simples de restaurar via reload
 * durante os testes E2E.
 */
@Named
@SessionScoped
public class ConfirmDemoBean implements Serializable {

    private List<String> itensBehavior = new ArrayList<>(List.of("Item A", "Item B", "Item C"));
    private List<String> itensAtributo = new ArrayList<>(List.of("Item X", "Item Y", "Item Z"));

    public List<String> getItensBehavior() {
        return itensBehavior;
    }

    public List<String> getItensAtributo() {
        return itensAtributo;
    }

    public void excluirBehavior(String item) {
        itensBehavior.remove(item);
    }

    public void excluirAtributo(String item) {
        itensAtributo.remove(item);
    }

    public void restaurar() {
        itensBehavior = new ArrayList<>(List.of("Item A", "Item B", "Item C"));
        itensAtributo = new ArrayList<>(List.of("Item X", "Item Y", "Item Z"));
    }
}
