package fi.nls.hakunapi.csv;

import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;

public class CSVFeatureCollectionWriter extends CSVFeatureWriterBase implements FeatureCollectionWriter {

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {
        HakunaProperty id = ft.getId();
        HakunaPropertyGeometry geom = ft.getGeom();
        List<String> columns = ft.getProperties().stream()
                .filter(it -> it.getPropertyWriter() != HakunaPropertyWriters.HIDDEN)
                .filter(it -> ALLOWED_TYPES.contains(it.getType()))
                .map(it -> it.getName())
                .collect(Collectors.toList());
        int len = columns.size() + (geom == null ? 1 : 2);

        String[] cols = new String[len];
        int i = 0;
        cols[i++] = id.getName();
        if (geom != null) {
            cols[i++] = geom.getName();
        }
        for (String col : columns) {
            cols[i++] = col;
        }
        csv.init(cols);
    }

    @Override
    public void endFeatureCollection() throws Exception {
        // NOP
    }

    @Override
    public void startFeature(String fid) throws Exception {
        csv.writeString(fid);
    }

    @Override
    public void startFeature(long fid) throws Exception {
        csv.writeNumber(fid);
    }

    @Override
    public void startFeature(int fid) throws Exception {
        csv.writeNumber(fid);
    }

    @Override
    public void endFeature() throws Exception {
        // NOP
    }

}
