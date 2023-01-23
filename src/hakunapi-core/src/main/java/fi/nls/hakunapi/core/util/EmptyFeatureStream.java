package fi.nls.hakunapi.core.util;

import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.ValueProvider;

public class EmptyFeatureStream implements FeatureStream {

    @Override
    public void close() throws Exception {
        // NOP
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public ValueProvider next() {
        return null;
    }

}
