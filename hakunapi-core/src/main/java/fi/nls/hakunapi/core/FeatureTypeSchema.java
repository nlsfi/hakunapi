package fi.nls.hakunapi.core;

import java.util.Map;

import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaPropertyType;

public class FeatureTypeSchema {

    private final HakunaPropertyType idType;
    private final HakunaGeometryType geomType;
    private final Map<String, HakunaPropertyType> propertyTypes;

    public FeatureTypeSchema(HakunaPropertyType idType, HakunaGeometryType geomType,
            Map<String, HakunaPropertyType> propertyTypes) {
        this.idType = idType;
        this.geomType = geomType;
        this.propertyTypes = propertyTypes;
    }

    public HakunaPropertyType getIdType() {
        return idType;
    }

    public HakunaGeometryType getGeomType() {
        return geomType;
    }

    public Map<String, HakunaPropertyType> getPropertyTypes() {
        return propertyTypes;
    }

}
