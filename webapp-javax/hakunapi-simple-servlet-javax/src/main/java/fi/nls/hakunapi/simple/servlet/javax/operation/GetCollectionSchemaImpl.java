package fi.nls.hakunapi.simple.servlet.javax.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schema.JsonSchemaUtil;
import fi.nls.hakunapi.core.schemas.GeoJSONGeometrySchema;
import fi.nls.hakunapi.core.schemas.OAS30toJsonSchema;
import fi.nls.hakunapi.core.schemas.SchemaDefinition;
import fi.nls.hakunapi.simple.servlet.javax.MediaTypes;
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
            @QueryParam("lang") String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String id = String.format("%s/collections/%s/schema", service.getCurrentServerURL(headers::getHeaderString), collectionId);

        Map<String, Schema<?>> schemas = ft instanceof SimpleFeatureType
                ? ((SimpleFeatureType) ft).getLangToSchema() : null;

        String resolvedLang = null;
        Schema<?> collectionSchema = null;
        if (schemas != null && !schemas.isEmpty()) {
            if (langParam != null) {
                resolvedLang = resolveLang(schemas, langParam);
            } else {
                resolvedLang = headers.getAcceptableLanguages().stream()
                        .filter(l -> !l.equals(Locale.ROOT))
                        .map(l -> resolveLang(schemas, l.toLanguageTag()))
                        .filter(s -> s != null)
                        .findFirst()
                        .orElse(schemas.keySet().iterator().next());
            }
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

    private static String resolveLang(Map<String, Schema<?>> schemas, String lang) {
        String normalized = lang.toLowerCase();
        if (schemas.containsKey(normalized)) {
            return normalized;
        }
        int dash = normalized.indexOf('-');
        if (dash > 0) {
            String prefix = normalized.substring(0, dash);
            if (schemas.containsKey(prefix)) {
                return prefix;
            }
        }
        return null;
    }

}
