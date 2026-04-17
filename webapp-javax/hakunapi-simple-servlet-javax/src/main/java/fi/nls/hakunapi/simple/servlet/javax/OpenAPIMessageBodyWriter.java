package fi.nls.hakunapi.simple.servlet.javax;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import io.swagger.v3.core.jackson.mixin.*;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.links.LinkParameter;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.EncodingProperty;
// io.swagger.v3.oas.models.media.MediaType used via FQN to avoid collision with javax.ws.rs.core.MediaType
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.XML;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.oas.models.tags.Tag;

@Provider
@Produces(MediaTypes.APPLICATION_OPENAPI_V3)
public class OpenAPIMessageBodyWriter implements MessageBodyWriter<OpenAPI> {

    private final ObjectMapper om = createMapper();

    private static ObjectMapper createMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .changeDefaultPropertyInclusion(incl ->
                        incl.withValueInclusion(Include.NON_NULL))
                .addMixIn(ApiResponses.class, ExtensionsMixin.class)
                .addMixIn(Contact.class, ExtensionsMixin.class)
                .addMixIn(Encoding.class, ExtensionsMixin.class)
                .addMixIn(EncodingProperty.class, ExtensionsMixin.class)
                .addMixIn(Example.class, ExampleMixin.class)
                .addMixIn(ExternalDocumentation.class, ExtensionsMixin.class)
                .addMixIn(Link.class, ExtensionsMixin.class)
                .addMixIn(LinkParameter.class, ExtensionsMixin.class)
                .addMixIn(io.swagger.v3.oas.models.media.MediaType.class, MediaTypeMixin.class)
                .addMixIn(OAuthFlow.class, ExtensionsMixin.class)
                .addMixIn(OAuthFlows.class, ExtensionsMixin.class)
                .addMixIn(Operation.class, OperationMixin.class)
                .addMixIn(PathItem.class, ExtensionsMixin.class)
                .addMixIn(Paths.class, ExtensionsMixin.class)
                .addMixIn(Scopes.class, ExtensionsMixin.class)
                .addMixIn(Server.class, ExtensionsMixin.class)
                .addMixIn(ServerVariable.class, ExtensionsMixin.class)
                .addMixIn(ServerVariables.class, ExtensionsMixin.class)
                .addMixIn(Tag.class, ExtensionsMixin.class)
                .addMixIn(XML.class, ExtensionsMixin.class)
                .addMixIn(ApiResponse.class, ExtensionsMixin.class)
                .addMixIn(Parameter.class, ExtensionsMixin.class)
                .addMixIn(RequestBody.class, ExtensionsMixin.class)
                .addMixIn(Header.class, ExtensionsMixin.class)
                .addMixIn(SecurityScheme.class, ExtensionsMixin.class)
                .addMixIn(Callback.class, ExtensionsMixin.class)
                .addMixIn(Schema.class, SchemaMixin.class)
                .addMixIn(DateSchema.class, DateSchemaMixin.class)
                .addMixIn(Components.class, ComponentsMixin.class)
                .addMixIn(Info.class, InfoMixin.class)
                .addMixIn(License.class, LicenseMixin.class)
                .addMixIn(OpenAPI.class, OpenAPIMixin.class)
                .addMixIn(Discriminator.class, DiscriminatorMixin.class)
                .build();
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return OpenAPI.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(OpenAPI openAPI, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(OpenAPI openAPI, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                    throws IOException, WebApplicationException {
        om.writeValue(entityStream, openAPI);
    }
}
