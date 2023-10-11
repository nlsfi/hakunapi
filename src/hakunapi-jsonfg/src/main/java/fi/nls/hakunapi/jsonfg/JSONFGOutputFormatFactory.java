package fi.nls.hakunapi.jsonfg;

import java.util.Map;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatFactorySpi;

public class JSONFGOutputFormatFactory implements OutputFormatFactorySpi, JSONFG {

	@Override
	public boolean canCreate(Map<String, String> params) {
		return TYPE.equals(params.get("type"));
	}

	@Override
	public OutputFormat create(Map<String, String> params) {
		return JSONFGOutputFormat.INSTANCE;
	}

}
