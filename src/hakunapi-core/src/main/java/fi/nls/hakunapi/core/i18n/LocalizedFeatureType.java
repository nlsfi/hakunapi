package fi.nls.hakunapi.core.i18n;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.CacheSettings;
import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OrderBy;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schemas.Link;
import io.swagger.v3.oas.models.media.Schema;

/**
 * A FeatureType decorated with localized descriptive elements.
 */
public class LocalizedFeatureType implements FeatureType {

    private final FeatureType wrapped;
    private final String lang;
    private final Localization l10n;

    public LocalizedFeatureType(FeatureType wrapped, String lang, Localization l10n) {
        this.wrapped = wrapped;
        this.lang = lang;
        this.l10n = l10n;
    }

    public FeatureType unwrap() {
        return wrapped;
    }

    public String getLang() {
        return lang;
    }

    /* Localized */

    @Override
    public String getTitle() {
        String explicit = l10n.get(lang, LocalizableKeys.collectionTitle(getName()), null);
        if (explicit != null) {
            return explicit;
        }
        Schema<?> schema = getSchema();
        if (schema != null && schema.getTitle() != null) {
            return schema.getTitle();
        }
        return wrapped.getTitle();
    }

    @Override
    public String getDescription() {
        String explicit = l10n.get(lang, LocalizableKeys.collectionDescription(getName()), null);
        if (explicit != null) {
            return explicit;
        }
        Schema<?> schema = getSchema();
        if (schema != null && schema.getDescription() != null) {
            return schema.getDescription();
        }
        return wrapped.getDescription();
    }

    private Schema<?> getSchema() {
        Map<String, Schema<?>> langToSchema = getLangToSchema();
        if (langToSchema.isEmpty()) {
            return null;
        }
        String schemaLang = LangNegotiation.match(new ArrayList<>(langToSchema.keySet()), lang);
        return schemaLang == null ? null : langToSchema.get(schemaLang);
    }

    /* Delegated */

    @Override
    public List<Link> getAdditionalLinks() {
        return wrapped.getAdditionalLinks();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public String getNS() {
        return wrapped.getNS();
    }

    @Override
    public String getSchemaLocation() {
        return wrapped.getSchemaLocation();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return wrapped.getMetadata();
    }

    @Override
    public HakunaProperty getId() {
        return wrapped.getId();
    }

    @Override
    public HakunaPropertyGeometry getGeom() {
        return wrapped.getGeom();
    }

    @Override
    public List<HakunaProperty> getProperties() {
        return wrapped.getProperties();
    }

    @Override
    public List<HakunaProperty> getSchemaProperties() {
        return wrapped.getSchemaProperties();
    }

    @Override
    public List<HakunaProperty> getQueryableProperties() {
        return wrapped.getQueryableProperties();
    }

    @Override
    public List<DatetimeProperty> getDatetimeProperties() {
        return wrapped.getDatetimeProperties();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isWriteNullProperties() {
        return wrapped.isWriteNullProperties();
    }

    @Override
    public double[] getSpatialExtent() {
        return wrapped.getSpatialExtent();
    }

    @Override
    public Instant[] getTemporalExtent() {
        return wrapped.getTemporalExtent();
    }

    @Override
    public List<GetFeatureParam> getParameters() {
        return wrapped.getParameters();
    }

    @Override
    public List<GetFeatureParam> getConformanceParams(List<GetFeatureParam> conformanceSpecificParams) {
        return wrapped.getConformanceParams(conformanceSpecificParams);
    }

    @Override
    public List<Filter> getStaticFilters() {
        return wrapped.getStaticFilters();
    }

    @Override
    public ProjectionTransformerFactory getProjectionTransformerFactory() {
        return wrapped.getProjectionTransformerFactory();
    }

    @Override
    public boolean isSourceWillProject() {
        return wrapped.isSourceWillProject();
    }

    @Override
    public PaginationStrategy getPaginationStrategy() {
        return wrapped.getPaginationStrategy();
    }

    @Override
    public List<OrderBy> getDefaultOrderBy() {
        return wrapped.getDefaultOrderBy();
    }

    @Override
    public FeatureProducer getFeatureProducer() {
        return wrapped.getFeatureProducer();
    }

    @Override
    public CacheSettings getCacheSettings() {
        return wrapped.getCacheSettings();
    }

    @Override
    public Map<String, Schema<?>> getLangToSchema() {
        return wrapped.getLangToSchema();
    }

    /* Identity follows the wrapped instance */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalizedFeatureType) {
            return wrapped.equals(((LocalizedFeatureType) obj).wrapped);
        }
        return wrapped.equals(obj);
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }

}
