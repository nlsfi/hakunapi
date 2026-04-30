package fi.nls.hakunapi.simple.servlet.jakarta;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    // JavaTimeModule (jsr310) is now built-in in Jackson 3
    // WRITE_DATES_AS_TIMESTAMPS defaults to false in Jackson 3
    // WRITE_ENUMS_USING_TO_STRING defaults to true in Jackson 3
    final ObjectMapper om = JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl ->
                    incl.withValueInclusion(Include.NON_NULL))
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return om;
    }

}
