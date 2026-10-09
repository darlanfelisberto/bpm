package br.com.feliva.bpm.converter;

import br.com.feliva.sharedClass.db.Model;
import br.edu.iffar.box.converter.EntityResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class ModelEntityResolver implements EntityResolver {

    @Inject
    private EntityManager em;

    @Override
    public <T> T find(Class<T> entityClass, Object id) {
        return em.find(entityClass, id);
    }

    @Override
    public Object getId(Object entity) {
        if (entity instanceof Model<?> model) {
            return model.getMMId();
        }
        return EntityResolver.super.getId(entity);
    }
}
