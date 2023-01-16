package fi.nls.hakunapi.esbulk;

import com.fasterxml.jackson.core.io.SerializedString;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class ESbulkSingleFeatureWriter extends ESbulkFeatureWriterBase implements SingleFeatureWriter {

    @Override
    public void startFeature(FeatureType ft, String layername, String fid) throws Exception {
        initGeometryWriter(ft);

        writeHeader(new SerializedString(layername));

        w.writeStartObject();
        w.writeFieldName(Strings.ID);
        w.writeString(fid);
    }

    @Override
    public void startFeature(FeatureType ft, String layername, long fid) throws Exception {
        initGeometryWriter(ft);

        writeHeader(new SerializedString(layername));

        w.writeStartObject();
        w.writeFieldName(Strings.ID);
        w.writeNumber(fid);
    }

    @Override
    public void endFeature() throws Exception {
        w.writeEndObject();
    }

}
