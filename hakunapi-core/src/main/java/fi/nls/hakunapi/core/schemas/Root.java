package fi.nls.hakunapi.core.schemas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class Root implements Component {

    private final String title;
    private final String description;
    private final List<Link> links;

    public Root() {
        title = null;
        description = null;
        links = null;
    }

    private Root(String title, String description, List<Link> links) {
        this.title = title;
        this.description = description;
        this.links = links;
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

    @Override
    public String getComponentName() {
        return "root";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema()
                    .name(getComponentName())
                    .addProperties("title", new StringSchema())
                    .addProperties("description", new StringSchema())
                    .addProperties("links", new ArraySchema().items(new Link().toSchema(components).name("links")));
            schema.setRequired(Collections.singletonList("links"));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

    public static class Builder {

        private final String title;
        private final String description;
        private final String base;
        private final String query;
        private final List<Link> links;

        public Builder(String title, String description, String url, String query, String contentType) {
            this.title = title;
            this.description = description;
            this.base = url;
            this.query = query;
            this.links = new ArrayList<>();

            String href = base + "/" + query;
            String rel = "self";
            String linkTitle = "This document";

            this.links.add(new Link(href, rel, contentType, linkTitle));
        }

        public Builder alternate(String contentType) {
            String href = base + "/" + query;
            String rel = "alternate";
            String title = "This document";
            links.add(new Link(href, rel, contentType, title));
            return this;
        }

        public Builder api(String contentType) {
            String href = base + "/api" + query;
            String rel = "service-desc";
            String title = "the API definition";
            links.add(new Link(href, rel, contentType, title));
            return this;
        }

        public Builder conformance(String contentType) {
            String href = base + "/conformance" + query;
            String rel = "conformance";
            String title = "OGC API conformance classes implemented by this server";
            links.add(new Link(href, rel, contentType, title));
            return this;
        }

        public Builder collections(String contentType) {
            String href = base + "/collections" + query;
            String rel = "data";
            String title = "Information about the feature collections";
            links.add(new Link(href, rel, contentType, title));
            return this;
        }

        public Root build() {
            return new Root(title, description, links);
        }

    }

}
