package fi.nls.hakunapi.gpkg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public class OutputFormatGPKG implements OutputFormat {
    
    public static final String MEDIA_TYPE = "application/geopackage+vnd.sqlite3";
    public static final String ID = "gpkg";
    
    private final File dir;
    
    public OutputFormatGPKG(File dir) {
        this.dir = dir;
    }
    
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getMimeType() {
        return MEDIA_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new GPKGFeatureCollectionWriter(dir);
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new GPKGSingleFeatureWriter(dir);
    }

    @Override
    public Map<String, String> getResponseHeaders(GetFeatureRequest request) {
        String filename;
        if (request.getCollections().size() == 1) {
            filename = request.getCollections().get(0).getName() + ".gpkg";
        } else {
            filename = "items.gpkg";
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", getMimeType());
        headers.put("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        return headers;
    }

    @Override
    public String getMediaMainType() {
        return "application";
    }

    @Override
    public String getMediaSubType() {
        return "geopackage+vnd.sqlite3";
    }

}
