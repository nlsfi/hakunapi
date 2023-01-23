package fi.nls.hakunapi.core.param;

import java.util.List;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class LayernamesParam implements GetFeatureParam {

    public static final String PARAM_NAME = "layernames";

    private static final String DESCRIPTION = "The value of the layernames parameter is a "
            + "comma-separated list of names to be used instead of collection identifiers "
            + "for example in MVT files. For example &collections=foo,bar&layernames=baz,qux "
            + "would get features from collections foo but would name the layer as baz.";

    @Override
    public String getParamName() {
        return PARAM_NAME;
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new StringSchema());
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }
        List<GetFeatureCollection> collections = request.getCollections();
        String[] layernames = value.split(",");
        int n = Math.min(collections.size(), layernames.length);
        for (int i = 0; i < n; i++) {
            GetFeatureCollection c = collections.get(i);
            c.setLayername(layernames[i]);
        }
    }

}
