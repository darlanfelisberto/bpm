package br.edu.iffar.box.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Universal entity converter that serializes entities to a compact
 * classHashCode@entityId representation and reconstructs them by delegating
 * database lookups to an application-provided {@link EntityResolver}.
 */
@Named("entityConverter")
@ApplicationScoped
@FacesConverter(value = "box.entityConverter")
public class EntityConverter implements Converter<Object> {

    private static final Logger LOGGER = Logger.getLogger(EntityConverter.class.getName());

    public static final String CONVERTER_ID = "box.entityConverter";
    public static final String SEPARATOR = "@";

    private static final Map<String, Class<?>> CLASS_REGISTRY = new ConcurrentHashMap<>();

    @Inject
    private Instance<EntityResolver> resolverInstance;

    public EntityConverter() {
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank() || !value.contains(SEPARATOR)) {
            return null;
        }

        int separatorIndex = value.indexOf(SEPARATOR);
        try {
            String classToken = value.substring(0, separatorIndex);
            String idStr = value.substring(separatorIndex + 1);

            Class<?> entityClass = CLASS_REGISTRY.get(classToken);

            if (entityClass == null) {
                LOGGER.log(Level.WARNING, "Entity class not registered or found for token: " + classToken);
                return null;
            }

            EntityResolver resolver = getResolver();
            if (resolver == null) {
                return null;
            }

            Object id = parseId(entityClass, idStr);
            return id != null ? resolver.find(entityClass, id) : null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to resolve entity for classKey@id: " + value, e);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return null;
        }

        EntityResolver resolver = getResolver();
        Object id = resolver != null ? resolver.getId(value) : null;
        if (id == null) {
            return null;
        }

        Class<?> entityClass = value.getClass();
        if (entityClass.getName().contains("$$") || entityClass.getName().contains("HibernateProxy")) {
            Class<?> superclass = entityClass.getSuperclass();
            if (superclass != null && !Object.class.equals(superclass)) {
                entityClass = superclass;
            }
        }
        String classKey = String.valueOf(entityClass.getName().hashCode());
        CLASS_REGISTRY.putIfAbsent(classKey, entityClass);

        return classKey + SEPARATOR + id;
    }

    public static void register(Class<?> clazz) {
        if (clazz != null) {
            CLASS_REGISTRY.put(String.valueOf(clazz.getName().hashCode()), clazz);
        }
    }

    protected EntityResolver getResolver() {
        if (resolverInstance != null && !resolverInstance.isUnsatisfied()) {
            return resolverInstance.get();
        }
        try {
            Instance<EntityResolver> cdiInstance = CDI.current().select(EntityResolver.class);
            if (cdiInstance.isResolvable()) {
                return cdiInstance.get();
            }
            if (!cdiInstance.isUnsatisfied()) {
                return cdiInstance.iterator().next();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "CDI resolution of EntityResolver failed: " + e.getMessage(), e);
        }
        return null;
    }

    private Object parseId(Class<?> entityClass, String idStr) {
        if (idStr == null || idStr.isBlank()) {
            return null;
        }
        Class<?> idType = resolveIdType(entityClass);
        if (UUID.class.equals(idType)) {
            return UUID.fromString(idStr);
        } else if (Long.class.equals(idType) || long.class.equals(idType)) {
            return Long.valueOf(idStr);
        } else if (Integer.class.equals(idType) || int.class.equals(idType)) {
            return Integer.valueOf(idStr);
        }
        return idStr; //string provavelmente
    }

    private Class<?> resolveIdType(Class<?> entityClass) {
        for (String methodName : new String[]{"getMMId","getId"}) {
            for (Method method : entityClass.getMethods()) {
                if (methodName.equals(method.getName()) && !method.isBridge() && method.getParameterCount() == 0) {
                    Class<?> ret = method.getReturnType();
                    if (!Object.class.equals(ret)) {
                        return ret;
                    }
                }
            }
        }
        return String.class;
    }
}
