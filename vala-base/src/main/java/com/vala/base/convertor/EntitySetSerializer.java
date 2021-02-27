package com.vala.base.convertor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vala.base.entity.BaseEntity;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonComponent
public class EntitySetSerializer {

    public static class SetSerializer
            extends JsonSerializer<Set<BaseEntity>> {
//        @PersistenceContext
//        private EntityManager entityManager;
        @Override
        public void serialize(Set<BaseEntity> baseEntities, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {



            jsonGenerator.writeStartArray();
            try {
                for (BaseEntity baseEntity : baseEntities) {
                    jsonGenerator.writeObject(baseEntity.getId());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            jsonGenerator.writeEndArray();
        }
        public int[] getArray(Set<BaseEntity> baseEntities){
            int[] arr = new int[baseEntities.size()];
            int i = 0;
            for (BaseEntity baseEntity : baseEntities) {
                arr[i++] = baseEntity.getId();
            }
            return arr;
        }

    }

    public static class SetDeserializer
            extends JsonDeserializer<Set<BaseEntity>> {

        @Override
        public Set<BaseEntity> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            Set<BaseEntity> list = new HashSet<>();
            int[] ints = jsonParser.getCodec().readValue(jsonParser, int[].class);
            for (int anInt : ints) {
                BaseEntity entity = new BaseEntity();
                entity.setId(anInt);
                list.add(entity);
            }
            return list;
        }
    }


}
