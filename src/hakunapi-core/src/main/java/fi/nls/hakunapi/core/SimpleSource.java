package fi.nls.hakunapi.core;

import java.nio.file.Path;

import fi.nls.hakunapi.core.config.HakunaConfigParser;

public interface SimpleSource extends AutoCloseable {

    public String getType(); 
    public SimpleFeatureType parse(HakunaConfigParser cfg, Path path, String collectionId, int[] srids) throws Exception;

    public default void close() throws Exception {
        // NOP
    }

}
