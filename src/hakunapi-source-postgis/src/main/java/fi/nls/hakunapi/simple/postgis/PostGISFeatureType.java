package fi.nls.hakunapi.simple.postgis;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.sql.SQLFeatureType;

public class PostGISFeatureType extends SQLFeatureType {

    @Override
    public FeatureProducer getFeatureProducer() {
        return new SimplePostGIS(ds);
    }

}
