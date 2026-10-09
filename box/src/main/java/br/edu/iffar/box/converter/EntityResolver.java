package br.edu.iffar.box.converter;

/**
 * Strategy interface for looking up entities by class and primary key,
 * and extracting the primary key value from an entity instance.
 */
public interface EntityResolver {

    /**
     * Finds an entity instance by its type and identifier.
     *
     * @param entityClass the entity class
     * @param id the primary key identifier
     * @param <T> the entity type
     * @return the matching entity instance, or null if not found
     */
    <T> T find(Class<T> entityClass, Object id);

    /**
     * Extracts the primary key identifier from an entity instance.
     *
     * @param entity the entity instance
     * @return the identifier, or null
     */
    default Object getId(Object entity) {
        if (entity == null) {
            return null;
        }
        for (String methodName : new String[]{"getMMId", "getId"}) {
            try {
                return entity.getClass().getMethod(methodName).invoke(entity);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
