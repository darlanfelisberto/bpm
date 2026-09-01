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
 * Converts Person <-> id (String) so b:datatable can reconstruct the
 * selected row on demand (the "select" event), without keeping the row
 * anywhere between requests. managed=true makes JSF fetch the instance
 * via CDI (instead of instantiating by reflection) when used as
 * <f:converter converterId="personConverter">; the explicit @Dependent
 * is what actually registers the class as a CDI bean (the WAR has no
 * beans.xml - implicit "annotated" discovery only picks up classes with
 * a scope annotation, @FacesConverter alone doesn't count), which also
 * enables the direct by-type @Inject used in DatatableDemoBean. In a
 * real application, a repository/DAO would sit here instead of the
 * DatatableDemoBean injected below.
 */
@Named
@Dependent
public class PersonConverter implements Converter<Person>, Serializable {

    @Inject
    private DatatableDemoBean datatableDemoBean;

    private DatatableDemoBean resolveBean(FacesContext context) {
        if (datatableDemoBean != null) {
            return datatableDemoBean;
        }
        return (DatatableDemoBean) context.getApplication().getELResolver()
                .getValue(context.getELContext(), null, "datatableDemoBean");
    }

    @Override
    public Person getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            DatatableDemoBean bean = resolveBean(context);
            return bean != null ? bean.findById(Long.parseLong(value)) : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Person value) {
        return value == null || value.getId() == null ? "" : String.valueOf(value.getId());
    }
}
