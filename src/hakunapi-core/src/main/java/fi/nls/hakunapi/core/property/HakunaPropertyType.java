package fi.nls.hakunapi.core.property;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import fi.nls.hakunapi.core.geom.HakunaGeometry;

public enum HakunaPropertyType {

    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    STRING(String.class),
    GEOMETRY(HakunaGeometry.class),
    DATE(LocalDate.class),
    TIMESTAMP(LocalDateTime.class),
    TIMESTAMPTZ(Instant.class),
    BOOLEAN(Boolean.class),
    UUID(UUID.class),
    ARRAY(Object[].class),
    JSON(Object.class);

    public final Class<?> jClass;
    
    private HakunaPropertyType(Class<?> jClass) {
        this.jClass = jClass;
    }

}
