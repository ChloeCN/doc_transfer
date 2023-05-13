package org.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JsonUtil {
    private static ObjectMapper objectMapper;

    static {
        JsonUtil.objectMapper = new ObjectMapper();
        JsonUtil.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        JsonUtil.objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES,false);
    }

    public static String format(final Object object){
        try {
            return JsonUtil.objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.info("object2Json Exception:",e);
            return null;
        }
    }

    public static <T> T parse(final String json,final Class<T> ref){
        try {
            return JsonUtil.objectMapper.readValue(json,ref);
        } catch (JsonProcessingException e) {
            log.info("Json2object Exception:",e);
        }
        return null;
    }

    public static <T> T parse(final String json,final TypeReference<?> ref){
        try {
            return (T) JsonUtil.objectMapper.readValue(json,ref);
        } catch (JsonProcessingException e) {
            log.info("Json2object Exception:",e);
        }
        return null;
    }
}
