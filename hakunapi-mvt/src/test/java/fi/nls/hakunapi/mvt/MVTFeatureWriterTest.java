package fi.nls.hakunapi.mvt;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;

import com.wdtinc.mapbox_vector_tile.VectorTile;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.PaginationStrategyCursor;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.util.SimplifyAlgorithm;

public class MVTFeatureWriterTest {
    
    static int SRID = 0;
    
    @Test
    public void test() throws Exception {
        Envelope bbox = new Envelope(0, 100, 0, 100);
        int extent = 4096;
        int buffer = 256;
        double tolerance = 2.0;
        
        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(200, 20),
                new Coordinate(200, 200),
                new Coordinate(20, 200),
                new Coordinate(0, 0)
        };
        
        GeometryFactory gf = HakunaGeometryFactory.GF;

        int numLayers = 10;
        
        FeatureType ft = new FeatureType() {
            @Override
            public String getTitle() {
                return null;
            }
            
            @Override
            public String getSchemaLocation() {
                return null;
            }
            
            @Override
            public List<HakunaProperty> getProperties() {
                return null;
            }
            
            @Override
            public String getName() {
                return null;
            }
            
            @Override
            public String getNS() {
                return null;
            }
            
            @Override
            public HakunaProperty getId() {
                return null;
            }
            
            @Override
            public HakunaPropertyGeometry getGeom() {
                return null;
            }
            
            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public ProjectionTransformerFactory getProjectionTransformerFactory() {
                return null;
            }

            @Override
            public double[] getSpatialExtent() {
                return null;
            }

            @Override
            public Instant[] getTemporalExtent() {
                return null;
            }

            @Override
            public List<HakunaProperty> getQueryableProperties() {
                return null;
            }

            @Override
            public Map<String, Object> getMetadata() {
                return null;
            }

            @Override
            public List<Filter> getStaticFilters() {
                return Collections.emptyList();
            }

            @Override
            public List<DatetimeProperty> getDatetimeProperties() {
                return null;
            }

            @Override
            public boolean isWriteNullProperties() {
                return false;
            }

            @Override
            public List<GetFeatureParam> getParameters() {
                return null;
            }

            @Override
            public HakunaGeometryDimension getGeomDimension() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PaginationStrategyCursor getPaginationStrategy() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public FeatureProducer getFeatureProducer() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FeatureCollectionWriter mvt = new HakunaMVTFeatureWriter(bbox, extent, buffer, tolerance, SimplifyAlgorithm.DP)) {
            for (int i = 0; i < numLayers; i++) {
                mvt.init(baos, 0, SRID);
                mvt.startFeatureCollection(ft, "layer_" + i);
                mvt.startFeature(12345);
                mvt.writeGeometry("g", new HakunaGeometryJTS(gf.createPolygon(coordinates)));
                mvt.writeProperty("foo", "bar");
                mvt.writeProperty("baz", 12345);
                mvt.endFeature();
                mvt.endFeatureCollection();
                mvt.end(false, Collections.emptyList(), 1);
            }
        }
        byte[] b = baos.toByteArray();

        VectorTile.Tile tile = VectorTile.Tile.parseFrom(b);
        assertEquals(numLayers, tile.getLayersCount());
        for (int i = 0; i < numLayers; i++) {
            VectorTile.Tile.Layer layer = tile.getLayers(i);
            assertEquals("layer_" + i, tile.getLayers(i).getName());
            assertEquals(2, layer.getKeysCount());
            assertEquals("foo", layer.getKeys(0));
            assertEquals("baz", layer.getKeys(1));
            assertEquals(2, layer.getValuesCount());
            assertEquals(true, layer.getValues(0).hasStringValue());
            assertEquals("bar", layer.getValues(0).getStringValue());
            assertEquals(true, layer.getValues(1).hasSintValue());
            assertEquals(12345, layer.getValues(1).getSintValue());
        }
    }

}
