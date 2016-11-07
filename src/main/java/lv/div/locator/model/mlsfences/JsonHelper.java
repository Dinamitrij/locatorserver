package lv.div.locator.model.mlsfences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;

/**
 * Helper class for JSON deserialize
 */
public class JsonHelper<T> implements Serializable {

    private static final long serialVersionUID = -6859425136310651104L;

    private ObjectMapper jsonObjectMapper;

    public JsonHelper() {
        this.jsonObjectMapper = new ObjectMapper();
    }

    public T buildPojo(String sourceJson, Class<T> cls) {
        try {
            return jsonObjectMapper.readValue(sourceJson, cls);
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] buildJsonForMQ(Object src) {
        try {
            return jsonObjectMapper.writeValueAsBytes(src);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}