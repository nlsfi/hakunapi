package fi.nls.hakunapi.source.gpkg;

import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.schemas.Link;

public class InMemoryFeatureWriter implements FeatureCollectionWriter {

    public List<List<Object>> written;
    public List<Object> current;

    @Override
    public void close() throws Exception {

    }

    @Override
    public void init(OutputStream out, SRIDCode srid) throws Exception {
        // NOP
    }

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {
        written = new ArrayList<>();
    }

    @Override
    public void endFeatureCollection() throws Exception {
        // NOP
    }

    @Override
    public void startFeature(String fid) throws Exception {
        current = new ArrayList<>();
        current.add(fid);
    }

    @Override
    public void startFeature(long fid) throws Exception {
        current = new ArrayList<>();
        current.add(fid);
    }

    @Override
    public void endFeature() throws Exception {
        written.add(current);
        current = null;
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
    public void writeAttribute(String name, String value) throws Exception {
        writeProperty(name, value);
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        writeProperty(name, geometry);
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        current.add(geometry);
    }

    @Override
    public void writeProperty(String name, String value) throws Exception {
        current.add(value);
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        current.add(value);
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        current.add(value);
    }

    @Override
    public void writeProperty(String name, boolean value) throws Exception {
        current.add(value);
    }

    @Override
    public void writeProperty(String name, int value) throws Exception {
        current.add(value);
    }

    @Override
    public void writeProperty(String name, long value) throws Exception {
        current.add(value);
    }

    @Override
    public void writeProperty(String name, float value) throws Exception {
        current.add(value);
    }

    @Override
    public void writeProperty(String name, double value) throws Exception {
        current.add(value);
    }

    @Override
    public void writeNullProperty(String name) throws Exception {
        current.add(null);
    }

    @Override
    public void writeStartObject(String name) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeCloseObject() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeStartArray(String name) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeCloseArray() throws Exception {
        throw new UnsupportedOperationException();
    }

}
