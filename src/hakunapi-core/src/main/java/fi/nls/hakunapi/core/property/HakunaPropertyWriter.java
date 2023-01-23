package fi.nls.hakunapi.core.property;

import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.ValueProvider;

@FunctionalInterface
public interface HakunaPropertyWriter {

    public void write(ValueProvider vp, int i, FeatureWriter writer) throws Exception;

}
