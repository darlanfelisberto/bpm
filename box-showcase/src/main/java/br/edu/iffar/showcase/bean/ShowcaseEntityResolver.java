package br.edu.iffar.showcase.bean;

import br.edu.iffar.box.converter.EntityResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;

/**
 * Showcase implementation of EntityResolver to demonstrate box.entityConverter
 * in memory without a database dependency.
 */
@ApplicationScoped
public class ShowcaseEntityResolver implements EntityResolver {

    private DatatableDemoBean resolveBean() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        }
        return (DatatableDemoBean) context.getApplication().getELResolver()
                .getValue(context.getELContext(), null, "datatableDemoBean");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T find(Class<T> entityClass, Object id) {
        if (Person.class.equals(entityClass) && id != null) {
            DatatableDemoBean bean = resolveBean();
            if (bean != null) {
                long personId = id instanceof Number n ? n.longValue() : Long.parseLong(id.toString());
                return (T) bean.findById(personId);
            }
        }
        return null;
    }
}
