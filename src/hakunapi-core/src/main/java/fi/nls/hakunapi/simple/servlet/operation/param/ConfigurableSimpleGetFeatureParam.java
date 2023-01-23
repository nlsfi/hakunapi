package fi.nls.hakunapi.simple.servlet.operation.param;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.property.HakunaProperty;

public class ConfigurableSimpleGetFeatureParam extends SimpleGetFeatureParam {

    private final String name;
    private final String description;

    public ConfigurableSimpleGetFeatureParam(FeatureType ft, HakunaProperty property, FilterMapper filterMapper, String name, String description) {
        super(ft, property, filterMapper);
        this.name = name != null ? name : super.getParamName();
        this.description = description != null ? description : super.getDescription();
    }

    @Override
    public String getParamName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }

}
