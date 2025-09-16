package fi.nls.hakunapi.core.schemas;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
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
                    .addProperty("href", new StringSchema().name("href"))
                    .addProperty("rel", new StringSchema().name("rel").example("prev"))
                    .addProperty("type", new StringSchema().name("type").example("application/geo+json"))
                    .addProperty("title", new StringSchema().name("title"))
                    .addProperty("hreflang", new StringSchema().name("hreflang").example("en"));
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

    @JsonIgnore
    public String toLinkHeader() {
        StringBuilder sb = new StringBuilder(64);
        appendURI(sb, href);
        sb.append("; rel=").append(rel).append('"');
        sb.append("; type=").append(type).append('"');
        if (title != null && !title.isBlank()) {
            sb.append("; title=").append(title).append('"');
        }
        if (hreflang != null && !hreflang.isBlank()) {
            sb.append("; hreflang=").append(hreflang).append('"');
        }
        return sb.toString();
    }

    private static StringBuilder appendURI(StringBuilder sb, String uri) {
        final int n = uri.length();
        final char[] arr = new char[2];
        final CharBuffer buf = CharBuffer.wrap(arr);

        sb.append('<');
        for (int i = 0; i < n;) {
            int c = uri.charAt(i);
            if (c <= 255) {
                sb.append((char) c);
                i++;
            } else {
                arr[0] = (char) c;
                if (c >= 0xD800 && c <= 0xDBFF) {
                    if (i + 1 < n) {
                        int d = uri.charAt(i + 1);
                        if (d >= 0xDC00 && d <= 0xDFFF) {
                            arr[1] = (char) d;
                            i++;
                        }
                    }
                }
                i++;
                ByteBuffer bb = StandardCharsets.UTF_8.encode(buf);
                while (bb.hasRemaining()) {
                    byte b = bb.get();
                    sb.append('%');
                    char ch = Character.forDigit((b >> 4) & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= 32;
                    }
                    sb.append(ch);
                    ch = Character.forDigit(b & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= 32;
                    }
                    sb.append(ch);
                }
            }
        }
        sb.append('>');
        return sb;
    }

}
