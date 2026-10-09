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

/**
 * Universal entity converter that serializes entities to a compact
 * classHashCode@entityId representation and reconstructs them by delegating
 * database lookups to an application-provided {@link EntityResolver}.
 */
@Named("entityConverter")
@ApplicationScoped
@FacesConverter(value = "box.entityConverter", managed = true)
public class EntityConverter implements Converter<Object> {

    public static final String CONVERTER_ID = "box.entityConverter";
    public static final String SEPARATOR = "@";

    private static final Map<Integer, Class<?>> CLASS_REGISTRY = new ConcurrentHashMap<>();

    @Inject
    private Instance<EntityResolver> resolverInstance;

    private EntityResolver fallbackResolver;

    public EntityConverter() {
    }

    public EntityConverter(EntityResolver resolver) {
        this.fallbackResolver = resolver;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank() || !value.contains(SEPARATOR)) {
            return null;
        }

        int separatorIndex = value.indexOf(SEPARATOR);
        if (separatorIndex <= 0 || separatorIndex >= value.length() - 1) {
            return null;
        }

        try {
            int classKey = Integer.parseInt(value.substring(0, separatorIndex));
            String idStr = value.substring(separatorIndex + 1);

            Class<?> entityClass = CLASS_REGISTRY.get(classKey);
            if (entityClass == null) {
                return null;
            }

            EntityResolver resolver = getResolver();
            if (resolver == null) {
                return null;
            }

            Object id = parseId(entityClass, idStr);
            return id != null ? resolver.find(entityClass, id) : null;
        } catch (Exception e) {
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
        int classKey = entityClass.getName().hashCode();
        CLASS_REGISTRY.putIfAbsent(classKey, entityClass);

        return classKey + SEPARATOR + id;
    }

    public static void register(Class<?> clazz) {
        if (clazz != null) {
            CLASS_REGISTRY.put(clazz.getName().hashCode(), clazz);
        }
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.fallbackResolver = resolver;
    }

    protected EntityResolver getResolver() {
        if (fallbackResolver != null) {
            return fallbackResolver;
        }
        if (resolverInstance != null && !resolverInstance.isUnsatisfied()) {
            return resolverInstance.get();
        }
        try {
            Instance<EntityResolver> cdiInstance = CDI.current().select(EntityResolver.class);
            if (cdiInstance.isResolvable()) {
                return cdiInstance.get();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Object parseId(Class<?> entityClass, String idStr) {
        Class<?> idType = resolveIdType(entityClass);
        if (UUID.class.equals(idType)) {
            return UUID.fromString(idStr);
        } else if (Long.class.equals(idType) || long.class.equals(idType)) {
            return Long.valueOf(idStr);
        } else if (Integer.class.equals(idType) || int.class.equals(idType)) {
            return Integer.valueOf(idStr);
        }
        return idStr;
    }

    private Class<?> resolveIdType(Class<?> entityClass) {
        for (String methodName : new String[]{"getMMId", "getId"}) {
            try {
                Method method = entityClass.getMethod(methodName);
                return method.getReturnType();
            } catch (NoSuchMethodException ignored) {
            }
        }
        return String.class;
    }
}
