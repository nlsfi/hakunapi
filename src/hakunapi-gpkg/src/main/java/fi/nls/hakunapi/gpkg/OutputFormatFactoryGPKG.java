package fi.nls.hakunapi.gpkg;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatFactorySpi;

public class OutputFormatFactoryGPKG implements OutputFormatFactorySpi {

    @Override
    public boolean canCreate(Map<String, String> params) {
        if (!OutputFormatGPKG.ID.equals(params.get("type"))) {
            return false;
        }
        String _dir = params.get("dir");
        if (_dir == null) {
            return false;
        }
        File dir = new File(_dir);
        if (!dir.isDirectory()) {
            return false;
        }
        try {
            File tmp = File.createTempFile("test", ".gpkg", dir);
            tmp.delete();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public OutputFormat create(Map<String, String> params) {
        String _dir = params.get("dir");
        File dir = new File(_dir);
        return new OutputFormatGPKG(dir);
    }

}
