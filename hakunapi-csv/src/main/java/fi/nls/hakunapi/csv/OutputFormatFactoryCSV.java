package fi.nls.hakunapi.csv;

import java.util.Map;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatFactorySpi;

public class OutputFormatFactoryCSV implements OutputFormatFactorySpi {

    @Override
    public boolean canCreate(Map<String, String> params) {
        return OutputFormatCSV.ID.equals(params.get("type"));
    }

    @Override
    public OutputFormat create(Map<String, String> params) {
        return OutputFormatCSV.INSTANCE;
    }

}
