package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.property.HakunaProperty;

public class DatetimeProperty {

    private final HakunaProperty property;
    private final boolean inclusive;

    public DatetimeProperty(HakunaProperty property, boolean inclusive) {
        this.property = property;
        this.inclusive = inclusive;
    }

    public HakunaProperty getProperty() {
        return property;
    }

    public boolean isInclusive() {
        return inclusive;
    }

}
