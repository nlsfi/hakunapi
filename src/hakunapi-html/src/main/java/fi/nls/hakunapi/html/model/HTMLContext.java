package fi.nls.hakunapi.html.model;

import java.util.Collections;
import java.util.List;

import fi.nls.hakunapi.core.FeatureServiceConfig;

public class HTMLContext<T> {

    private final FeatureServiceConfig service;
    private final String basePath;
    private final String basePathTrailingSlash;
    private final T model;
    private final HTMLLanguageContext language;

    public HTMLContext(FeatureServiceConfig service, String basePath, T model) {
        this(service, basePath, model, null, Collections.emptyList());
    }

    /**
     * @param lang resolved language, null if this resource is not localized
     * @param availableLanguages every language this resource can be served in,
     *        including lang; templates render the others as alternates
     */
    public HTMLContext(FeatureServiceConfig service, String basePath, T model, String lang,
            List<String> availableLanguages) {
        this.service = service;
        this.basePath = basePath;
        this.basePathTrailingSlash = basePath + (basePath.endsWith("/") ? "" : "/");
        this.model = model;
        this.language = new HTMLLanguageContext();
        this.language.setLang(lang);
        this.language.setAvailableLanguages(availableLanguages);
    }

    public FeatureServiceConfig getService() {
        return service;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getBasePathTrailingSlash() {
        return basePathTrailingSlash;
    }

    public T getModel() {
        return model;
    }

    /**
     * @return the language chrome for the templates: resolved language,
     *         switchable languages and the query fragment carrying it
     */
    public HTMLLanguageContext getLanguage() {
        return language;
    }

    /** @return shorthand for getLanguage().getLang(), for the html lang attribute */
    public String getLang() {
        return language.getLang();
    }

    public List<String> getAvailableLanguages() {
        return language.getAvailableLanguages();
    }

    public List<String> getAlternateLanguages() {
        return language.getAlternateLanguages();
    }

    public String getLangQuery() {
        return language.getLangQuery();
    }

}
