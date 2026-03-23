package fi.nls.hakunapi.simple.servlet.javax;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.core.util.Json;

@Provider
@Produces(value = { MediaTypes.APPLICATION_OPENAPI_V3, MediaTypes.APPLICATION_SCHEMA })
public class OpenAPIObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return Json.mapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

}