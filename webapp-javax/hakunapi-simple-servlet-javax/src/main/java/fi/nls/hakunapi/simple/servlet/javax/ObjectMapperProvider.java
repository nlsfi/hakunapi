package fi.nls.hakunapi.simple.servlet.javax;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

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
