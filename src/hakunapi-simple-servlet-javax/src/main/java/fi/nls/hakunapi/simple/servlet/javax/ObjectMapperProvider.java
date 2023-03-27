package fi.nls.hakunapi.simple.servlet.javax;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper om = init();

    private ObjectMapper init() {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(Include.NON_NULL);
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.setDateFormat(new StdDateFormat());
        om.registerModule(new JavaTimeModule());
        
        return om;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return om;
    }

}