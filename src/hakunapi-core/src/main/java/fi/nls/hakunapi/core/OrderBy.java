package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.property.HakunaProperty;

public class OrderBy {

    private final HakunaProperty property;
    private final boolean ascending;

    public OrderBy(HakunaProperty property, boolean ascending) {
        this.property = property;
        this.ascending = ascending;
    }

    public HakunaProperty getProperty() {
        return property;
    }

    public boolean isAscending() {
        return ascending;
    }

}
