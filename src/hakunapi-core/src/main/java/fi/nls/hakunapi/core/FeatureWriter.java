package fi.nls.hakunapi.core;

import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;

public interface FeatureWriter extends AutoCloseable {

    public String getMimeType();
    public int getSrid();

    @Deprecated
    /**
     * @deprecated use init(OutputStream, int, int, boolean) instead
     */
    public default void init(OutputStream out, int maxDecimalsCoordinate, int srid) throws Exception {
        init(out, maxDecimalsCoordinate, srid, false);
    }

    public default void init(OutputStream out, int maxDecimalsCoordinate, int srid, boolean crsIsLatLon) throws Exception {
        int minDecimalsFloat = 0;
        int maxDecimalsFloat = 5;
        int minDecimalsDouble = 0;
        int maxDecimalsDouble = 8;
        int minDecimalsOrdinate = 0;
        FloatingPointFormatter f = new DefaultFloatingPointFormatter(
                minDecimalsFloat,
                maxDecimalsFloat,
                minDecimalsDouble,
                maxDecimalsDouble,
                minDecimalsOrdinate,
                maxDecimalsCoordinate);
        init(out, f, srid, crsIsLatLon);
    };

    public default void init(OutputStream out, FloatingPointFormatter formatter, int srid, boolean crsIsLatLon) throws Exception {
        init(out, formatter, srid);
    }

    public void init(OutputStream out, FloatingPointFormatter formatter, int srid) throws Exception;

    public default void initGeometryWriter(HakunaGeometryDimension dims) {};
    
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception;

    public void writeTimeStamp() throws Exception;
    public void writeLinks(List<Link> link) throws Exception;
    public void writeNumberReturned(int numberReturned) throws Exception;
    public default void writeMetadata(String key, Map<String, Object> metadata) throws Exception {};

    public void writeAttribute(String name, String value) throws Exception;

    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception;
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception;
    public void writeProperty(String name, String value) throws Exception;
    public void writeProperty(String name, LocalDate value) throws Exception;
    public void writeProperty(String name, Instant value) throws Exception;
    public void writeProperty(String name, boolean value) throws Exception;
    public void writeProperty(String name, int value) throws Exception;
    public void writeProperty(String name, long value) throws Exception;
    public void writeProperty(String name, float value) throws Exception;
    public void writeProperty(String name, double value) throws Exception;
    public void writeNullProperty(String name) throws Exception;

    public void writeStartObject(String name) throws Exception;
    public void writeCloseObject() throws Exception;

    public void writeStartArray(String name) throws Exception;
    public void writeCloseArray() throws Exception;
    public default void  writeJsonProperty(String name, byte[] bytes) throws Exception {};
}
