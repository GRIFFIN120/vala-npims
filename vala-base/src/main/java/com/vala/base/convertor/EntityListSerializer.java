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
import java.util.List;

@JsonComponent
public class EntityListSerializer {

    public static class ListSerializer
            extends JsonSerializer<List<BaseEntity>> {
//        @PersistenceContext
//        private EntityManager entityManager;
        @Override
        public void serialize(List<BaseEntity> baseEntities, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {



            jsonGenerator.writeStartArray();
            try {
                for (BaseEntity baseEntity : baseEntities) {
                    jsonGenerator.writeObject(baseEntity.getId());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            jsonGenerator.writeEndArray();
        }
        public int[] getArray(List<BaseEntity> baseEntities){
            int[] arr = new int[baseEntities.size()];
            for (int i : arr) {
                arr[i] = baseEntities.get(i).getId();
            }
            return arr;
        }

    }

    public static class ListDeserializer
            extends JsonDeserializer<List<BaseEntity>> {

        @Override
        public List<BaseEntity> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            List<BaseEntity> list = new ArrayList<>();
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
