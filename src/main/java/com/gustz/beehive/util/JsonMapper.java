package com.gustz.beehive.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * Jackson ObjectMapper extend
 *
 * @author ZHENFENG ZHANG
 * @since [Apr 16, 2015]
 */
public abstract class JsonMapper {

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // Uses Enum.toString() for serialization of an Enum--write
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        // Uses Enum.toString() for deserialization of an Enum--read
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        mapper.setSerializationInclusion(Include.NON_NULL);
        //this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //this.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static String writeValueAsString(Object bean) {
        return writeValueAsString(false, bean);
    }

    public static String writeValueAsString(final boolean isFormat, Object bean) {
        try {
            if (isFormat) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bean);
            } else {
                return mapper.writeValueAsString(bean);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T readValue(String json, Class<T> retType) {
        try {
            return mapper.readValue(json, retType);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void setDateFormat(String pattern) {
        mapper.setDateFormat(new SimpleDateFormat(pattern));
    }

}
