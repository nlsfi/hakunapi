package fi.nls.hakunapi.core.schemas;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class CollectionsContent implements Component {

    private final List<Link> links;
    private final List<CollectionInfo> collections;

    public CollectionsContent() {
        this.links = null;
        this.collections = null;
    }

    public CollectionsContent(List<Link> links, List<CollectionInfo> collections) {
        this.links = Objects.requireNonNull(links);
        this.collections = Objects.requireNonNull(collections);
    }

    @Override
    public String getComponentName() {
        return "content";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema()
                    .name(getComponentName())
                    .addProperties("links", new ArraySchema().items(new Link().toSchema(components)))
                    .addProperties("collections", new ArraySchema().items(new CollectionInfo().toSchema(components)));
            schema.setRequired(Arrays.asList("links", "collections"));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

    public List<Link> getLinks() {
        return links;
    }

    public List<CollectionInfo> getCollections() {
        return collections;
    }

}
