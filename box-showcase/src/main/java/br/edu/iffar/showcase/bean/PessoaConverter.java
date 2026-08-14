package br.edu.iffar.showcase.bean;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Converte Pessoa <-> id (String) pro b:datatable reconstruir a linha
 * selecionada sob demanda (evento "select"), sem guardar a linha em
 * nenhum lugar entre requisições. managed=true faz o JSF buscar a
 * instância via CDI (em vez de instanciar por reflection) quando usado
 * como <f:converter converterId="pessoaConverter">; o @Dependent explícito
 * é o que de fato registra a classe como bean CDI (o WAR não tem
 * beans.xml - descoberta implícita via "annotated" só pega classe com
 * anotação de escopo, @FacesConverter sozinho não conta), permitindo
 * também o @Inject direto por tipo usado em DatatableDemoBean. Numa
 * aplicação de verdade, aqui entraria um repositório/DAO no lugar do
 * DatatableDemoBean injetado abaixo.
 */
@Named
public class PessoaConverter implements Converter<Pessoa>, Serializable {

    @Inject
    private DatatableDemoBean datatableDemoBean;

    @Override
    public Pessoa getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return datatableDemoBean.buscarPorId(Long.parseLong(value));
        } catch (NumberFormatException erro) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Pessoa value) {
        return value == null || value.getId() == null ? "" : String.valueOf(value.getId());
    }
}
