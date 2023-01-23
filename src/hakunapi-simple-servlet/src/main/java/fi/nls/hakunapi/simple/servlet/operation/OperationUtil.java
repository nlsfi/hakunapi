package fi.nls.hakunapi.simple.servlet.operation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.util.U;

public class OperationUtil {

    public static String getQuery(WFS3Service service, UriInfo uriInfo) {
        return U.toQuery(getQueryParams(service, uriInfo));
    }

    public static Map<String, String> getQueryParams(WFS3Service service, UriInfo uriInfo) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        if (service.getApiKeyQueryParam() != null) {
            String v = uriInfo.getQueryParameters().getFirst(service.getApiKeyQueryParam());
            if (v != null && !v.isEmpty()) {
                queryParams.put(service.getApiKeyQueryParam(), v);
            }
        }
        return queryParams;
    }

    public static OutputFormat determineOutputFormat(Request request, Collection<OutputFormat> outputFormats) {
        List<Variant> variants = outputFormats.stream()
                .map(f -> new MediaType(f.getMediaMainType(), f.getMediaSubType(), f.getMimeParameters()))
                .map(mediaType -> new Variant(mediaType, (String) null, (String) null))
                .collect(Collectors.toList());
        Variant bestVariant = request.selectVariant(variants);
        if (bestVariant != null) {
            MediaType reqMediaType = bestVariant.getMediaType();
            for (OutputFormat f : outputFormats) {
                MediaType fMediaType = new MediaType(f.getMediaMainType(), f.getMediaSubType(), f.getMimeParameters());
                if (reqMediaType.isCompatible(fMediaType)) {
                    return f;
                }
            }
        }
        return null;
    }

}
