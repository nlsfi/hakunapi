package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.Collections;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
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
    public SchemaDefinition handle(
            @PathParam("collectionId") String collectionId,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            return null;
        }

        String id = String.format("%s/collections/%s/schema", service.getCurrentServerURL(headers::getHeaderString), collectionId);
        String title = "Schema for " + (ft.getTitle() != null ? ft.getTitle() : ft.getId());
        String description = "JSON Schema describing the properties for each feature";

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
        for (HakunaProperty p : ft.getSchemaProperties()) {
            if (p.getType() == HakunaPropertyType.GEOMETRY) {
                HakunaGeometryType type = ((HakunaPropertyGeometry) p).getGeometryType();
                properties.addProperties(p.getName(), GeoJSONGeometrySchema.getSchema(type));
            } else {
                Schema<?> schema = p.getSchema();
                if (schema != null) {
                    properties.addProperties(p.getName(), OAS30toJsonSchema.toJsonSchema(schema));
                }
            }
        }
        definition.addProperty("properties", properties);

        return definition;
    }

}
