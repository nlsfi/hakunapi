package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Variant;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.i18n.LangNegotiation;
import fi.nls.hakunapi.core.util.U;

public class OperationUtil {

    public static String getQuery(FeatureServiceConfig service, UriInfo uriInfo) {
        return U.toQuery(getQueryParams(service, uriInfo));
    }

    /**
     * Accept-Language tags in order of preference, as plain strings for
     * LangNegotiation. The JAX-RS wildcard maps to Locale.ROOT, whose tag is
     * "und"; LangNegotiation treats that as no preference.
     */
    public static List<String> getAcceptableLanguages(HttpHeaders headers) {
        return headers.getAcceptableLanguages().stream()
                .map(Locale::toLanguageTag)
                .collect(Collectors.toList());
    }

    /**
     * Resolves the language for a resource that does not localize its own
     * content but still has to propagate the language through the links it
     * renders, so that navigating away from a localized page does not silently
     * drop the language.
     *
     * @return the resolved language, or null if the service is not localized
     */
    public static String resolveLang(FeatureServiceConfig service, String langParam, HttpHeaders headers) {
        return LangNegotiation.resolve(service.getLanguages(), langParam, getAcceptableLanguages(headers));
    }

    public static Map<String, String> getQueryParams(FeatureServiceConfig service, UriInfo uriInfo) {
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

    public static Map<String, String> toSimpleMap(MultivaluedMap<String, String> requestHeaders) {
        // Create case insensitive map
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        requestHeaders.forEach((k, v) -> headers.put(k, v.get(0)));
        return headers;
    }

}
