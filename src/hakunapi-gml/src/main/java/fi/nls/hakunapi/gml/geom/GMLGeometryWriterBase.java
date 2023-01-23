package fi.nls.hakunapi.gml.geom;

import javax.xml.stream.XMLStreamWriter;

import fi.nls.hakunapi.core.FloatingPointFormatter;

public abstract class GMLGeometryWriterBase {

    protected final XMLStreamWriter xml;
    protected final FloatingPointFormatter formatter;
    protected final String fid;
    protected final String property;
    protected final char[] buf;

    public GMLGeometryWriterBase(XMLStreamWriter xml, FloatingPointFormatter formatter, String fid, String property) {
        this.xml = xml;
        this.formatter = formatter;
        this.fid = fid;
        this.property = property;
        this.buf = new char[32];
    }
    
    protected String format(double[] xy) {
        StringBuilder sb = new StringBuilder();
        for (double d : xy) {
            sb.append(buf, 0, formatter.writeOrdinate(d, buf, 0));
            sb.append(' ');
        }
        // Remove last whitespace
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    protected String format(double x, double y, boolean first) {
        StringBuilder sb = new StringBuilder();
        if (!first) {
            sb.append(' ');
        }
        
        sb.append(buf, 0, formatter.writeOrdinate(x, buf, 0));
        sb.append(' ');
        sb.append(buf, 0, formatter.writeOrdinate(y, buf, 0));

        return sb.toString();
    }
    
    protected String format(double x, double y, double z, boolean first) {
        StringBuilder sb = new StringBuilder();
        
        if (!first) {
            sb.append(' ');
        }
        sb.append(buf, 0, formatter.writeOrdinate(x, buf, 0));
        sb.append(' ');
        sb.append(buf, 0, formatter.writeOrdinate(y, buf, 0));
        sb.append(' ');
        sb.append(buf, 0, formatter.writeOrdinate(z, buf, 0));

        return sb.toString();
    }
    
    protected String format(double x, double y, double z, double m, boolean first) {
        StringBuilder sb = new StringBuilder();

        if (!first) {
            sb.append(' ');
        }
        sb.append(buf, 0, formatter.writeOrdinate(x, buf, 0));
        sb.append(' ');
        sb.append(buf, 0, formatter.writeOrdinate(y, buf, 0));
        sb.append(' ');
        sb.append(buf, 0, formatter.writeOrdinate(z, buf, 0));
        sb.append(' ');
        sb.append(buf, 0, formatter.writeOrdinate(m, buf, 0));

        return sb.toString();
    }
    
    protected String getSrsName(int srid) {
        if (srid == 4326) {
            return "CRS:84";
        }
        return "EPSG:" + srid;
    }

    public abstract void init(int srid, int dimension) throws Exception;
    public abstract void end() throws Exception;

    public abstract void writeCoordinate(double x, double y) throws Exception;
    public abstract void writeCoordinate(double x, double y, double z) throws Exception;
    public abstract void writeCoordinate(double x, double y, double z, double m) throws Exception;

    public abstract void startRing() throws Exception;
    public abstract void endRing() throws Exception;
    
}
