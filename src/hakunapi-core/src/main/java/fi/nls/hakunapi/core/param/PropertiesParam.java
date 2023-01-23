package fi.nls.hakunapi.core.param;

import java.util.List;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class PropertiesParam implements GetFeatureParam {

    private static final String DESCRIPTION = "Select only these properties. Unknown properties are ignored";

    @Override
    public String getParamName() {
        return "properties";
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
        for (GetFeatureCollection c : request.getCollections()) {
            FeatureType ft = c.getFt();
            List<HakunaProperty> properties = GetFeatureCollection.getPropertiesBase(ft);
            List<HakunaProperty> original = ft.getProperties();
            for (String propertyName : value.split(",")) {
                for (HakunaProperty o : original) {
                    if (o.getName().equals(propertyName)) {
                        properties.add(o);
                        break;
                    }
                }
            }
            c.setProperties(properties);
        }
    }

}
