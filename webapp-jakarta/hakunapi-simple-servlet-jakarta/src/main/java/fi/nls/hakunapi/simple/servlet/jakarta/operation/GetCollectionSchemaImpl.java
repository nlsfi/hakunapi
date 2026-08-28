package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.Map;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.i18n.LangNegotiation;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schema.JsonSchemaUtil;
import fi.nls.hakunapi.core.param.LangParam;
import fi.nls.hakunapi.core.schemas.GeoJSONGeometrySchema;
import fi.nls.hakunapi.core.schemas.OAS30toJsonSchema;
import fi.nls.hakunapi.core.schemas.SchemaDefinition;
import fi.nls.hakunapi.simple.servlet.jakarta.MediaTypes;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

@Path("/collections")
public class GetCollectionSchemaImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Path("/{collectionId}/schema")
    @Produces(MediaTypes.APPLICATION_SCHEMA)
    @ResponseClass(SchemaDefinition.class)
    public Response handle(
            @PathParam("collectionId") String collectionId,
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String id = String.format("%s/collections/%s/schema", service.getCurrentServerURL(headers::getHeaderString), collectionId);

        Map<String, Schema<?>> schemas = ft.getLangToSchema();

        String resolvedLang = null;
        Schema<?> collectionSchema = null;
        if (!schemas.isEmpty()) {
            List<String> available = new ArrayList<>(schemas.keySet());
            List<String> acceptable = headers.getAcceptableLanguages().stream()
                    .map(Locale::toLanguageTag)
                    .collect(Collectors.toList());
            resolvedLang = LangNegotiation.resolve(available, langParam, acceptable);
            collectionSchema = schemas.get(resolvedLang);
        }

        String title = collectionSchema != null && collectionSchema.getTitle() != null
                ? collectionSchema.getTitle()
                : "Schema for " + (ft.getTitle() != null ? ft.getTitle() : ft.getId());
        String description = collectionSchema != null && collectionSchema.getDescription() != null
                ? collectionSchema.getDescription()
                : "JSON Schema describing the properties for each feature";

        SchemaDefinition definition = new SchemaDefinition(id, title, description);

        definition.addProperty("type", new StringSchema()._enum(Collections.singletonList("Feature")));
        definition.addProperty("id", ft.getId().getSchema());
        if (ft.getGeom() != null) {
            definition.addProperty("geometry", GeoJSONGeometrySchema.getSchema(ft.getGeom().getGeometryType()));
        } else {
            ObjectSchema nullSchema = new ObjectSchema();
            nullSchema.addEnumItemObject(null);
            definition.addProperty("geometry", nullSchema);
        }

        ObjectSchema properties = new ObjectSchema();
        List<String> required = new ArrayList<>();
        for (HakunaProperty p : ft.getSchemaProperties()) {
            if (p.getType() == HakunaPropertyType.GEOMETRY) {
                HakunaGeometryType type = ((HakunaPropertyGeometry) p).getGeometryType();
                properties.addProperty(p.getName(), GeoJSONGeometrySchema.getSchema(type));
            } else {
                Schema<?> schema = p.getSchema();
                if (schema != null) {
                    schema = OAS30toJsonSchema.toJsonSchema(schema);
                    if (collectionSchema != null) {
                        Schema<?> propLangSchema = JsonSchemaUtil.getPropertySchema(collectionSchema, p.getName());
                        if (propLangSchema != null) {
                            if (propLangSchema.getTitle() != null) schema.setTitle(propLangSchema.getTitle());
                            if (propLangSchema.getDescription() != null) schema.setDescription(propLangSchema.getDescription());
                        }
                    }
                    properties.addProperty(p.getName(), schema);
                }
            }
            if (!p.nullable()) {
                required.add(p.getName());
            }
        }
        if (!required.isEmpty()) {
            properties.setRequired(required);
        }
        definition.addProperty("properties", properties);

        Response.ResponseBuilder rb = Response.ok(definition);
        if (resolvedLang != null) {
            rb.header("Content-Language", resolvedLang);
        }
        return rb.build();
    }

}
