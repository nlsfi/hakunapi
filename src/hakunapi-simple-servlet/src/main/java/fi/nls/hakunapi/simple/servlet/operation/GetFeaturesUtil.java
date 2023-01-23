package fi.nls.hakunapi.simple.servlet.operation;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public class GetFeaturesUtil {

    public static void modify(WFS3Service service, GetFeatureRequest request, List<GetFeatureParam> params,
			MultivaluedMap<String, String> queryParams) {
		for (GetFeatureParam param : params) {
			modify(service, request, param, queryParams);
		}
	}

    public static void modify(WFS3Service service, GetFeatureRequest request, GetFeatureParam param,
			MultivaluedMap<String, String> queryParams) {
		String key = param.getParamName();
		String value = queryParams.getFirst(key);
		param.modify(service, request, value);
	}

}
