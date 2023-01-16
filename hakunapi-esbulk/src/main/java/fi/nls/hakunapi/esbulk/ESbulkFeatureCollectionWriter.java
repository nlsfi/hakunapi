package fi.nls.hakunapi.esbulk;

import com.fasterxml.jackson.core.io.SerializedString;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;

public class ESbulkFeatureCollectionWriter extends ESbulkFeatureWriterBase implements FeatureCollectionWriter {

    private SerializedString indexName;

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {
        this.indexName = new SerializedString(layername);
        initGeometryWriter(ft);
    }

    @Override
    public void endFeatureCollection() throws Exception {
        // NOP
    }

    @Override
    public void startFeature(String fid) throws Exception {
        writeHeader(indexName);

        w.writeStartObject();
        w.writeFieldName(Strings.ID);
        w.writeString(fid);
    }

    @Override
    public void startFeature(long fid) throws Exception {
        writeHeader(indexName);

        w.writeStartObject();
        w.writeFieldName(Strings.ID);
        w.writeNumber(fid);
    }

    @Override
    public void endFeature() throws Exception {
        w.writeEndObject();
    }

}
