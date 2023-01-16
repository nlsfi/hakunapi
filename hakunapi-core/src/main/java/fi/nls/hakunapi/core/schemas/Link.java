package fi.nls.hakunapi.core.schemas;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class Link implements Component {

    private final String href;
    private final String rel;
    private final String type;
    private final String title;
    private final String hreflang;

    public Link() {
        this.href = null;
        this.rel = null;
        this.type = null;
        this.title = null;
        this.hreflang = null;
    }

    public Link(String href, String rel, String type) {
        this(href, rel, type, null, null);
    }

    public Link(String href, String rel, String type, String title) {
        this(href, rel, type, title, null);
    }

    public Link(String href, String rel, String type, String title, String hreflang) {
        this.href = Objects.requireNonNull(href);
        this.rel = Objects.requireNonNull(rel);
        this.type = Objects.requireNonNull(type);
        this.title = title;
        this.hreflang = hreflang;
    }

    @Override
    @JsonIgnore
    public String getComponentName() {
        return "link";
    }

    @Override
    @JsonIgnore
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema()
                    .name(getComponentName())
                    .addProperties("href", new StringSchema().name("href"))
                    .addProperties("rel", new StringSchema().name("rel").example("prev"))
                    .addProperties("type", new StringSchema().name("type").example("application/geo+json"))
                    .addProperties("title", new StringSchema().name("title"))
                    .addProperties("hreflang", new StringSchema().name("hreflang").example("en"));
            schema.setRequired(Collections.singletonList("href"));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }

    public String getType() {
        return type;
    }

    public String getHreflang() {
        return hreflang;
    }

    public String getTitle() {
        return title;
    }

}
