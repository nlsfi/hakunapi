package fi.nls.hakunapi.html.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fi.nls.hakunapi.core.param.LangParam;

/**
 * The language chrome a template needs. Shared by HTMLContext and the feature
 * models, whose templates read the model at top level rather than through
 * ${model.} and so need these accessors themselves.
 */
public class HTMLLanguageContext {

    private String lang;
    private List<String> availableLanguages = Collections.emptyList();

    /** @return the resolved language, or null when the service is not localized */
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    /** @return every language this resource can be served in, in declared order */
    public List<String> getAvailableLanguages() {
        return availableLanguages;
    }

    public void setAvailableLanguages(List<String> availableLanguages) {
        this.availableLanguages = availableLanguages == null ? Collections.emptyList()
                : List.copyOf(availableLanguages);
    }

    /** @return the other languages this resource can be served in */
    public List<String> getAlternateLanguages() {
        List<String> alternates = new ArrayList<>();
        for (String available : availableLanguages) {
            if (!available.equals(lang)) {
                alternates.add(available);
            }
        }
        return alternates;
    }

    /** @return "?lang=fi", or "" when the service is not localized */
    public String getLangQuery() {
        return lang == null ? "" : "?" + LangParam.PARAM_NAME + "=" + lang;
    }

}
