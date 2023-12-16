package fi.nls.hakunapi.simple.webapp.javax;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import fi.nls.hakunapi.html.model.HTMLContext;

@Produces(MediaType.TEXT_HTML)
public class HTMLMessageBodyWriter implements MessageBodyWriter<Object> {

    private final Configuration cfg;
    private final Map<Class<?>, String> clazzToTemplate;

    public HTMLMessageBodyWriter(Configuration cfg) {
        this.cfg = cfg;
        this.clazzToTemplate = new HashMap<>();
    }

    public void add(Class<?> clazz, String template) {
        this.clazzToTemplate.put(clazz, template);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return mediaType.isCompatible(MediaType.TEXT_HTML_TYPE) 
                && type == HTMLContext.class
                && genericType instanceof ParameterizedType
                && clazzToTemplate.containsKey(((ParameterizedType) genericType).getActualTypeArguments()[0]);
    }
    
    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                    throws IOException, WebApplicationException {
        try {
            Type actual = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            Template template = cfg.getTemplate(clazzToTemplate.get(actual));
            Environment environment = template.createProcessingEnvironment(o, new OutputStreamWriter(entityStream, StandardCharsets.UTF_8));
            environment.setOutputEncoding(StandardCharsets.UTF_8.name());
            environment.process();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

}
