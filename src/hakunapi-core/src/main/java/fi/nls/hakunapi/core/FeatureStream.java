package fi.nls.hakunapi.core;

import java.util.Iterator;

public interface FeatureStream extends AutoCloseable, Iterator<ValueProvider> {
    
}
