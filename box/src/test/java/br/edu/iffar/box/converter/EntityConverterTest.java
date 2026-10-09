package br.edu.iffar.box.converter;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityConverterTest {

    static class SampleEntity {
        private Long id;
        private String name;

        SampleEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    static class SampleModel {
        private UUID mmId;
        private String title;

        SampleModel(UUID mmId, String title) {
            this.mmId = mmId;
            this.title = title;
        }

        public UUID getMMId() {
            return mmId;
        }

        public String getTitle() {
            return title;
        }
    }

    static class InMemoryResolver implements EntityResolver {
        private final Map<String, Object> store = new HashMap<>();

        void put(Class<?> clazz, Object id, Object entity) {
            store.put(clazz.getName() + ":" + id, entity);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T find(Class<T> entityClass, Object id) {
            return (T) store.get(entityClass.getName() + ":" + id);
        }
    }

    @Test
    void convertsEntityWithLongIdBothWays() {
        InMemoryResolver resolver = new InMemoryResolver();
        SampleEntity entity = new SampleEntity(42L, "Processo A");
        resolver.put(SampleEntity.class, 42L, entity);

        EntityConverter converter = new EntityConverter(resolver);

        String asString = converter.getAsString(null, null, entity);
        assertNotNull(asString);
        assertTrue(asString.endsWith("@42"));

        Object asObject = converter.getAsObject(null, null, asString);
        assertNotNull(asObject);
        assertInstanceOf(SampleEntity.class, asObject);
        assertEquals("Processo A", ((SampleEntity) asObject).getName());
    }

    @Test
    void convertsEntityWithUuidMMIdBothWays() {
        InMemoryResolver resolver = new InMemoryResolver();
        UUID uuid = UUID.randomUUID();
        SampleModel model = new SampleModel(uuid, "Autoavaliacao");
        resolver.put(SampleModel.class, uuid, model);

        EntityConverter converter = new EntityConverter(resolver);

        String asString = converter.getAsString(null, null, model);
        assertNotNull(asString);
        assertTrue(asString.endsWith("@" + uuid));

        Object asObject = converter.getAsObject(null, null, asString);
        assertNotNull(asObject);
        assertInstanceOf(SampleModel.class, asObject);
        assertEquals("Autoavaliacao", ((SampleModel) asObject).getTitle());
    }

    @Test
    void handlesNullAndInvalidValuesGracefully() {
        EntityConverter converter = new EntityConverter(new InMemoryResolver());

        assertNull(converter.getAsString(null, null, null));
        assertNull(converter.getAsObject(null, null, null));
        assertNull(converter.getAsObject(null, null, ""));
        assertNull(converter.getAsObject(null, null, "invalidStringWithoutSeparator"));
        assertNull(converter.getAsObject(null, null, "notAnInt@123"));
    }
}
