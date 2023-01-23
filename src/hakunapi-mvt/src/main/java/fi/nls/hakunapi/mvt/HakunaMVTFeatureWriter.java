package fi.nls.hakunapi.mvt;

import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.SimplifyAlgorithm;

public class HakunaMVTFeatureWriter implements FeatureCollectionWriter {

    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";
    public static final String STRING_ID_PROP = "_fid";

    private int extent;
    private MVTGeometryEncoder encoder;

    private VectorTile.Tile.Layer.Builder layerBuilder;
    private MVTFeatureBuilder featureBuilder;
    private MvtLayerProps layerProps;

    private OutputStream out;

    private int srid;
    
    public HakunaMVTFeatureWriter(Envelope tileEnvelope, int extent, int buffer, double tolerance, SimplifyAlgorithm alg) {
        this.extent = extent;
        this.encoder = new MVTGeometryEncoder(tileEnvelope, extent, buffer, tolerance, alg);
    }

    @Override
    public void init(OutputStream out, int maxDecimalsCoordinate, int srid) throws Exception {
        this.srid = srid;
        this.out = out;
    }

    @Override
    public void init(OutputStream out, FloatingPointFormatter formatter, int srid) throws Exception {
        this.srid = srid;
        this.out = out;
    }

    @Override
    public void close() throws Exception {
        if (out != null) {
            this.out.close();
        }
    }
    
    

    @Override
    public int getSrid() {
        return srid;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
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
    public void startFeatureCollection(FeatureType ft, String name) throws Exception {
        layerBuilder = VectorTile.Tile.Layer.newBuilder();
        layerBuilder.setVersion(2);
        layerBuilder.setName(name);
        layerBuilder.setExtent(extent);
        layerProps = new MvtLayerProps();
        featureBuilder = new MVTFeatureBuilder(layerProps);
    }

    @Override
    public void endFeatureCollection() throws Exception {
        MvtLayerBuild.writeProps(layerBuilder, layerProps);
        VectorTile.Tile.newBuilder().addLayers(layerBuilder.build()).build().writeTo(out);
        layerBuilder = null;
        featureBuilder = null;
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        // NOP
    }

    @Override
    public void startFeature(String fid) throws Exception {
        featureBuilder.addProperty(STRING_ID_PROP, fid);
    }

    @Override
    public void startFeature(long fid) throws Exception {
        featureBuilder.setId(fid);
    }

    @Override
    public void endFeature() throws Exception {
        VectorTile.Tile.Feature f = featureBuilder.toFeature();
        if (f != null) {
            layerBuilder.addFeatures(f);
        }
    }

    @Override
    public void writeAttribute(String name, String value) throws Exception {
        writeProperty(name, value);
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        if (geometry == null) {
            return;
        }
        Geometry g = encoder.toMVTGeom(geometry.toJTSGeometry());
        featureBuilder.setGeometry(g);
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        // NOP
    }

    @Override
    public void writeProperty(String name, String value) throws Exception {
        featureBuilder.addProperty(name, value);
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        if (value != null) {
            writeProperty(name, value.toString());
        }
    }
    
    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (value != null) {
            writeProperty(name, value.toString());
        }
    }

    @Override
    public void writeProperty(String name, boolean value) throws Exception {
        featureBuilder.addProperty(name, value);
    }

    @Override
    public void writeProperty(String name, int value) throws Exception {
        featureBuilder.addProperty(name, value);
    }

    @Override
    public void writeProperty(String name, long value) throws Exception {
        featureBuilder.addProperty(name, value);
    }

    @Override
    public void writeProperty(String name, float value) throws Exception {
        featureBuilder.addProperty(name, value);
    }

    @Override
    public void writeProperty(String name, double value) throws Exception {
        featureBuilder.addProperty(name, value);
    }

    @Override
    public void writeNullProperty(String name) throws Exception {
        // NOP
    }

    /*
    @Override
    public void writePropertyObject(String name, ObjectProperty value) throws Exception {
        // NOP
    }

    @Override
    public void writePropertyArray(String name, List<? extends ObjectProperty> list, String unitName,
            boolean repeatWrapper) throws Exception {
        // NOP
    }
     */

    @Override
    public void writeStartObject(String name) throws Exception {
        // NOP
    }

    @Override
    public void writeCloseObject() throws Exception {
        // NOP
    }

    @Override
    public void writeStartArray(String name) throws Exception {
        // NOP
    }

    @Override
    public void writeCloseArray() throws Exception {
        // NOP
    }

}
