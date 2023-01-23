package fi.nls.hakunapi.csv;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatCSV implements OutputFormat {

    public static final OutputFormat INSTANCE = new OutputFormatCSV();

    public static final String ID = "csv";
    public static final String MIME_TYPE = "text/csv; charset=UTF-8";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getMediaMainType() {
        return "text";
    }

    @Override
    public String getMediaSubType() {
        return "csv";
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new CSVFeatureCollectionWriter();
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new CSVSingleFeatureWriter();
    }

}
