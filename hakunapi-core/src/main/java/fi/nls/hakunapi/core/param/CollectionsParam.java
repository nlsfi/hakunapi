package fi.nls.hakunapi.core.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;

public class CollectionsParam implements GetFeatureParam {

    public static final String PARAM_NAME = "collections";
    private static final String DESCRIPTION = "The value of the collections the parameter is a "
            + "comma-separated list of collection identifiers enumerating the set of feature "
            + "collections (offered by a server) that should be accessed by a request. "
            + "Only features from the enumerated collections shall appear in the server’s response.";

    @Override
    public String getParamName() {
        return PARAM_NAME;
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        List<String> collections = service.getCollections().stream()
                .map(c -> c.getName())
                .collect(Collectors.toList());
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new StringSchema()._enum(collections));
    }

    public Collection<FeatureType> parse(WFS3Service service, String value) {
        if (value == null || value.isEmpty()) {
            return service.getCollections();
        }

        List<FeatureType> fts = new ArrayList<>();
        for (String v : value.split(",")) {
            FeatureType ft = service.getCollection(v);
            if (ft == null) {
                throw new IllegalArgumentException("Unknown collectionId");
            }
            fts.add(ft);
        }
        return fts;
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        Collection<FeatureType> fts;
        if (value == null || value.isEmpty()) {
            fts = service.getCollections();
        } else {
            fts = new ArrayList<>();
            for (String v : value.split(",")) {
                FeatureType ft = service.getCollection(v);
                if (ft == null) {
                    throw new IllegalArgumentException("Unknown collectionId");
                }
                fts.add(ft);
            }
            request.addQueryParam(getParamName(), value);
        }
        for (FeatureType ft : fts) {
            request.addCollection(new GetFeatureCollection(ft));
        }
    }

}
