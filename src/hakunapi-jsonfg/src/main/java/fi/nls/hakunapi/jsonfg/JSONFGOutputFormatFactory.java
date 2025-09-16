package fi.nls.hakunapi.jsonfg;

import java.util.Map;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatFactorySpi;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;
import fi.nls.hakunapi.geojson.hakuna.OutputFormatFactoryGeoJSON;

public class JSONFGOutputFormatFactory implements OutputFormatFactorySpi {

    @Override
    public boolean canCreate(Map<String, String> params) {
        return JSONFG.TYPE.equals(params.get("type"));
    }

    @Override
    public OutputFormat create(Map<String, String> params) {
        FloatingPointFormatter formatterDegrees = OutputFormatFactoryGeoJSON.parseFormatter(params.get("formatter.degrees"), DefaultFloatingPointFormatter.DEFAULT_DEGREES);
        FloatingPointFormatter formatterMeters = OutputFormatFactoryGeoJSON.parseFormatter(params.get("formatter.meters"), DefaultFloatingPointFormatter.DEFAULT_METERS);

        return new JSONFGOutputFormat(formatterDegrees, formatterMeters);
    }

}
