package fi.nls.hakunapi.csv;

import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.schemas.Link;

public abstract class CSVFeatureWriterBase implements FeatureWriter {

    protected static final EnumSet<HakunaPropertyType> ALLOWED_TYPES = EnumSet.of(
            HakunaPropertyType.BOOLEAN,
            HakunaPropertyType.DATE,
            HakunaPropertyType.DOUBLE,
            HakunaPropertyType.FLOAT,
            HakunaPropertyType.INT,
            HakunaPropertyType.LONG,
            HakunaPropertyType.STRING,
            HakunaPropertyType.TIMESTAMP,
            HakunaPropertyType.TIMESTAMPTZ,
            HakunaPropertyType.GEOMETRY,
            HakunaPropertyType.UUID
    );

    protected CSVWriter csv;
    protected int srid;
    protected int array;
    protected int object;


    @Override
    public String getMimeType() {
        return OutputFormatCSV.MIME_TYPE;
    }

    @Override
    public int getSrid() {
        return srid;
    }

    @Override
    public void init(OutputStream out, FloatingPointFormatter formatter, int srid) throws Exception {
        this.csv = new CSVWriter(out, formatter);
        this.srid = srid;
    }

    @Override
    public void close() throws Exception {
        this.csv.close();
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        // NOP
    }

    @Override
    public void writeTimeStamp() throws Exception {
        // NOP
    }

    @Override
    public void writeLinks(List<Link> link) throws Exception {
        // NOP
    }

    @Override
    public void writeNumberReturned(int numberReturned) throws Exception {
        // NOP
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        writeGeometry(name, geometry);
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        if (object == 0 && array == 0) {
            if (geometry == null) {
                writeNullProperty(name);
            } else {
                csv.writeGeometry(geometry);
            }
        }
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (value == null) {
            writeNullProperty(name);
        } else {
            writeProperty(name, value.toString());
        }
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        if (value == null) {
            writeNullProperty(name);
        } else {
            writeProperty(name, value.toString());
        }
    }

    @Override
    public void writeAttribute(String name, String value) throws Exception {
        writeProperty(name, value);
    }

    @Override
    public void writeProperty(String name, String value) throws Exception {
        if (object == 0 && array == 0) {
            if (value == null) {
                csv.writeNull();
            } else {
                csv.writeString(value);
            }
        }
    }

    @Override
    public void writeProperty(String name, boolean value) throws Exception {
        if (object == 0 && array == 0) {
            csv.writeBoolean(value);
        }
    }

    @Override
    public void writeProperty(String name, int value) throws Exception {
        if (object == 0 && array == 0) {
            csv.writeNumber(value);
        }
    }

    @Override
    public void writeProperty(String name, long value) throws Exception {
        if (object == 0 && array == 0) {
            csv.writeNumber(value);
        }
    }

    @Override
    public void writeProperty(String name, float value) throws Exception {
        if (object == 0 && array == 0) {
            csv.writeNumber(value);
        }
    }

    @Override
    public void writeProperty(String name, double value) throws Exception {
        if (object == 0 && array == 0) {
            csv.writeNumber(value);
        }
    }

    @Override
    public void writeNullProperty(String name) throws Exception {
        if (object == 0 && array == 0) {
            csv.writeNull();
        }
    }

    @Override
    public void writeStartObject(String name) throws Exception {
        object++;
    }

    @Override
    public void writeCloseObject() throws Exception {
        object--;
    }

    @Override
    public void writeStartArray(String name) throws Exception {
        array++;
    }

    @Override
    public void writeCloseArray() throws Exception {
        array--;
    }

}
