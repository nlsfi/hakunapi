package fi.nls.hakunapi.simple.servlet.operation.param;

import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.property.HakunaProperty;

public interface CustomizableGetFeatureParam extends GetFeatureParam {

    public void init(HakunaProperty prop, String name, String desc, String arg);

}
