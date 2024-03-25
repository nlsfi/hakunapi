package fi.nls.hakunapi.simple.sdo;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.simple.sdo.sql.SQLFeatureType;

public class SDOFeatureType extends SQLFeatureType {

	@Override
	public FeatureProducer getFeatureProducer() {
		return new SDOFeatureProducer(ds);
	}

}
