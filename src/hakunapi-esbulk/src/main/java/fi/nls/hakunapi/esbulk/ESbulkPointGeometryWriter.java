package fi.nls.hakunapi.esbulk;

import fi.nls.hakunapi.core.geom.HakunaGeometryType;

public class ESbulkPointGeometryWriter extends ESbulkGeometryWriter {

    public ESbulkPointGeometryWriter(String fieldName) {
        super(fieldName);
    }

    @Override
    public void init(HakunaGeometryType type, int srid, int dimension) throws Exception {
        if (type != HakunaGeometryType.POINT) {
            throw new IllegalArgumentException();
        }
        json.writeFieldName(fieldName);
    }

    @Override
    public void end() throws Exception {
        // NOP
    }

}
