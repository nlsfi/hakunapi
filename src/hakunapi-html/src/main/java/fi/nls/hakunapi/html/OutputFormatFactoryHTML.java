package fi.nls.hakunapi.html;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatFactorySpi;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

public class OutputFormatFactoryHTML implements OutputFormatFactorySpi {

    public static final String PARAM_TYPE = "type";
    public static final String PARAM_DIR = "dir";

    @Override
    public boolean canCreate(Map<String, String> params) {
        if (!OutputFormatHTML.ID.equals(params.get("type"))) {
            return false;
        }
        String dir = params.get(PARAM_DIR);
        if (dir == null) {
            return true;
        }

        File file = new File(dir);
        if (!file.isDirectory()) {
            return false;
        }
        try {
            new FileTemplateLoader(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public OutputFormat create(Map<String, String> params) {
        TemplateLoader loader = new ClassTemplateLoader(getClass(), "");

        if (params.containsKey(PARAM_DIR)) {
            try {
                TemplateLoader fallback = loader;
                TemplateLoader fileLoader = new FileTemplateLoader(new File(params.get(PARAM_DIR)));
                loader = new MultiTemplateLoader(new TemplateLoader[] { fileLoader, fallback });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateLoader(loader);

        OutputFormatHTMLSettings settings;
        Field[] fields = OutputFormatHTMLSettings.class.getDeclaredFields();
        if (Arrays.stream(fields).allMatch(f -> !params.containsKey(f.getName()))) {
            settings = OutputFormatHTMLSettings.OSM;
        } else {
            settings = new OutputFormatHTMLSettings();
            for (Field f : OutputFormatHTMLSettings.class.getDeclaredFields()) {
                String v = params.get(f.getName());
                if (v != null) {
                    try {
                        f.set(settings, v);
                    } catch (Exception ignore) {
                        // It's okay
                    }
                }
            }
        }

        return new OutputFormatHTML(cfg, settings);
    }

}
