package fi.nls.hakunapi.core.schemas;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class CollectionInfo extends ExtendableComponent {

    private final String id;
    private final String title;
    private final String description;
    private final List<Link> links;
    private final Extent extent;
    private final String itemType = "feature";
    private final String[] crs;
    private final String storageCrs;

    public CollectionInfo() {
        this.id = null;
        this.title = null;
        this.description = null;
        this.links = null;
        this.extent = null;
        this.crs = null;
        this.storageCrs = null;
    }

    public CollectionInfo(String id, String title, String description, List<Link> links, Extent extent, String[] crs, String storageCrs) {
        this.id = Objects.requireNonNull(id);
        this.title = title;
        this.description = description;
        this.links = Objects.requireNonNull(links);
        this.extent = extent != null && (extent.getSpatialExtent() != null || extent.getTemporalExtent() != null) ? extent : null;
        this.crs = crs;
        this.storageCrs = storageCrs;
    }

    @Override
    public String getComponentName() {
        return "collectionInfo";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema crs = new ArraySchema().items(new StringSchema());
            crs.setDefault(new String[] { Crs.CRS84 });
            Schema schema = new ObjectSchema()
                    .name(getComponentName())
                    .addProperties("id", new StringSchema().description("identifier of the collection used, for example, in URIs"))
                    .addProperties("title", new StringSchema().description("human readable title of the collection"))
                    .addProperties("description", new StringSchema().description("a description of the features in the collection"))
                    .addProperties("links", new ArraySchema().items(new Link().toSchema(components)))
                    .addProperties("extent", new Extent().toSchema(components))
                    .addProperties("itemType", new StringSchema()._default("feature").description("indicator about the type of the items in the collection"))
                    .addProperties("crs", crs)
                    .addProperties("storageCrs", new StringSchema().format("uri").description("the CRS identifier, from the list of supported CRS identifiers, that may be used to retrieve features from a collection without the need to apply a CRS transformation"))
                    ;
            addExtensionsToSchema(schema);
            schema.setRequired(Arrays.asList("id", "links"));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Extent getExtent() {
        return extent;
    }

    public String getItemType() {
        return itemType;
    }

    public String[] getCrs() {
        return crs;
    }

    public String getStorageCrs() {
        return storageCrs;
    }

}
