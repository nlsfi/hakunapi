package fi.nls.hakunapi.geojson.hakuna;

import java.util.Map;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatFactorySpi;

public class OutputFormatFactoryGeoJSON implements OutputFormatFactorySpi {

    @Override
    public boolean canCreate(Map<String, String> params) {
        return OutputFormatGeoJSON.TYPE.equals(params.get("type"));
    }

    @Override
    public OutputFormat create(Map<String, String> params) {
        return OutputFormatGeoJSON.INSTANCE;
    }

}
