package fi.nls.hakunapi.mvt;

import static org.junit.Assert.assertEquals;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.junit.Ignore;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import com.wdtinc.mapbox_vector_tile.VectorTile.Tile.Value;
import com.wdtinc.mapbox_vector_tile.adapt.jts.ITagConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtReader;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.core.util.SimplifyAlgorithm;

public class MVTGeometryEncoderTest {
    
    @Test
    public void basicTestLineString() throws Exception {
        Envelope bbox = new Envelope(2735834.1163830273,2736139.864496168,8617816.316983914,8618122.065097054);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        MVTGeometryEncoder e = new MVTGeometryEncoder(bbox, 4096, 16, 0.0, SimplifyAlgorithm.VW);
        
        LineString input = (LineString) wkt.read("LINESTRING(2735959.03293948 8618041.19489251,2736035.99626064 8617912.62380389,2736037.86525445 8617909.41317132,2736092.26505066 8617817.30356434,2736156.79768175 8617712.09851552,2736158.90672914 8617708.67845307,2736207.9400729 8617627.752061,2736208.82760509 8617626.27483666,2736261.65730254 8617538.02989555,2736263.07497642 8617535.67409647,2736307.77348066 8617461.74325048,2736308.22901675 8617460.9916944)");
        LineString output = (LineString) e.toMVTGeom(input);
        assertEquals(12, input.getNumPoints());
        assertEquals(5, output.getNumPoints());
    }
    
    @Test
    public void basicTestPolygon() throws Exception {
        Envelope bbox = new Envelope(2746841.048456095,2747146.796569236,8610478.362268537,8610784.110381678);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        MVTGeometryEncoder e = new MVTGeometryEncoder(bbox, 4096, 16, 2.0, SimplifyAlgorithm.VW);
        
        Polygon input = (Polygon) wkt.read("POLYGON((2747111.51322243 8610599.42448796, 2747082.98908636 8610587.03883319, 2746983.18564455 8610575.94135768, 2746982.75920532 8610616.36328615, 2747109.04992201 8610626.64363653, 2747111.51322243 8610599.42448796))"); 
        Polygon output = (Polygon) e.toMVTGeom(input);
        
        assertEquals(input.getExteriorRing().getNumPoints(), output.getExteriorRing().getNumPoints());
    }

    @Test
    @Ignore
    public void testRoikanvesi100k3857() throws Exception {
        JtsMvt mvt;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("592.pbf")) {
            mvt = MvtReader.loadMvt(in, HakunaGeometryFactory.GF, new ITagConverter() {
                @Override
                public Object toUserData(Long id, List<Integer> tags, List<String> keysList, List<Value> valuesList) {
                    return id;
                }
            });
        }
        JtsLayer layer = mvt.getLayers().iterator().next();
        Collection<Geometry> geometries = layer.getGeometries();
        int extent = 1024;
        BufferedImage bi = new BufferedImage(extent, extent, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, extent, extent);
        g2d.setStroke(new BasicStroke(4));
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 20));

        int i = 0;
        for (Geometry g : geometries) {
            g2d.setColor(getColor(i, geometries.size()));
            g2d.drawString(g.getUserData().toString(), 0, extent - (i * 20));
            i++;
            for (int j = 0; j < g.getNumGeometries(); j++) {
                Polygon polygon = (Polygon) g.getGeometryN(j);
                Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
                polygon.getExteriorRing().apply(new CoordinateSequenceFilter() {
                    @Override
                    public void filter(CoordinateSequence seq, int i) {
                        if (i == 0) {
                            path.moveTo(seq.getX(i), seq.getY(i));
                        } else {
                            path.lineTo(seq.getX(i), seq.getY(i));
                        }
                    }
                    @Override
                    public boolean isGeometryChanged() {
                        return false;
                    }
                    @Override
                    public boolean isDone() {
                        return false;
                    }
                });
                g2d.fill(path);
            }
        }
        g2d.dispose();
        File f = new File("C:/Omat/tmp/592.png");
        ImageIO.write(bi, "png", f);
        Desktop.getDesktop().open(f);
    }

    private Color getColor(int i, int size) {
        double percent = (double) i / size;
        int r = (int) (percent * 255);
        int g = (int) ((1.0 - percent) * 255);
        int b = (int) (percent * 255);
        return new Color(r, g, b);
    }

    @Test
    @Ignore
    public void testdJSON() throws Exception {
        byte[] b = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("roikanvesi_100k_3857.json").toURI()));
        String json = new String(b, StandardCharsets.UTF_8);
        FeatureCollection featureCollection = (FeatureCollection) GeoJSONFactory.create(json);

        // int zoom = 10;
        // int col = 592;
        // int row = 280;
        String bbox = "3130860.6785608195,9040360.209344367,3169996.4370428296,9079495.967826378";
        List<Double> bb = Arrays.stream(bbox.split(",")).map(Double::valueOf).collect(Collectors.toList());
        Envelope tileEnvelope = new Envelope(bb.get(0), bb.get(2), bb.get(1), bb.get(3));
        
        int extent = 1024;
        int buffer = 16;
        
        for (int tolerance = 1; tolerance <= 16; tolerance *= 2) {
            BufferedImage bi = new BufferedImage(extent, extent, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, extent, extent);
            g2d.setColor(Color.cyan);

            GeoJSONReader reader = new GeoJSONReader();
            MVTGeometryEncoder e = new MVTGeometryEncoder(tileEnvelope, extent, buffer, tolerance, SimplifyAlgorithm.VW);
            for (Feature f : featureCollection.getFeatures()) {
                if (f.getId().toString().equals("4012")) {
                    System.out.println("Hello");
                }
                Geometry g = reader.read(f.getGeometry());
                g = e.toMVTGeom(g);
                if (g == null) {
                    continue;
                }
                if (f.getId().toString().equals("4012")) {
                    Files.write(Paths.get("C:/Omat/tmp/wkt.txt"), g.toText().getBytes());
                }
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    draw(g2d, g.getGeometryN(i));
                }
            }
            g2d.dispose();
            File f = new File("C:/Omat/tmp/" + extent + "_vw_" + tolerance + ".png");
            ImageIO.write(bi, "png", f);
        }
    }
    
    private void draw(Graphics2D g2d, Geometry g) {
        if (g instanceof GeometryCollection) {
            for (int i = 0; i < g.getNumGeometries(); i++) {
                draw(g2d, g.getGeometryN(i));
            }
        } else {
            if (g instanceof Polygon) {
                Polygon p = (Polygon) g;
                Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
                addToPath((LinearRing) p.getExteriorRing(), path);
                for (int i = 0; i < p.getNumInteriorRing(); i++) {
                    addToPath((LinearRing) p.getInteriorRingN(i), path);
                }
                g2d.fill(path);
            }
        }
    }

    @Test
    @Ignore
    public void singlePerformanceTest() throws Exception {
        byte[] b = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("roikanvesi_100k_3857.json").toURI()));
        String json = new String(b, StandardCharsets.UTF_8);
        FeatureCollection featureCollection = (FeatureCollection) GeoJSONFactory.create(json);

        // int zoom = 10;
        // int col = 592;
        // int row = 280;
        String bbox = "3130860.6785608195,9040360.209344367,3169996.4370428296,9079495.967826378";
        List<Double> bb = Arrays.stream(bbox.split(",")).map(Double::valueOf).collect(Collectors.toList());
        Envelope tileEnvelope = new Envelope(bb.get(0), bb.get(2), bb.get(1), bb.get(3));

        int extent = 1024;
        int buffer = 16;

        GeoJSONReader reader = new GeoJSONReader();

        for (int tolerance = 1; tolerance <= 16; tolerance *= 2) {
            MVTGeometryEncoder e = new MVTGeometryEncoder(tileEnvelope, extent, buffer, tolerance, SimplifyAlgorithm.VW);
            for (Feature f : featureCollection.getFeatures()) {
                Geometry g = reader.read(f.getGeometry(), HakunaGeometryFactory.GF);
                g = e.toMVTGeom(g);
                if (g == null) {
                    continue;
                }
            }
        }
    }

    private void addToPath(LinearRing ls, Path2D path) {
        ls.apply(new CoordinateSequenceFilter() {
            @Override
            public void filter(CoordinateSequence seq, int i) {
                if (i == 0) {
                    path.moveTo(seq.getX(i), seq.getY(i));
                } else {
                    path.lineTo(seq.getX(i), seq.getY(i));
                }
            }
            @Override
            public boolean isGeometryChanged() {
                return false;
            }
            @Override
            public boolean isDone() {
                return false;
            }
        });
        path.closePath();
    }
    
    @Test
    @Ignore
    public void testJSON() throws Exception {
        byte[] b = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("roikanvesi_100k_3857.json").toURI()));
        String json = new String(b, StandardCharsets.UTF_8);
        FeatureCollection featureCollection = (FeatureCollection) GeoJSONFactory.create(json);

        int extent = 1024;
        BufferedImage bi = new BufferedImage(extent, extent, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, extent, extent);
        g2d.setStroke(new BasicStroke(4));
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 10));

        // int zoom = 10;
        // int col = 592;
        // int row = 280;
        String bbox = "3130860.6785608195,9040360.209344367,3169996.4370428296,9079495.967826378";
        List<Double> bb = Arrays.stream(bbox.split(",")).map(Double::valueOf).collect(Collectors.toList());
        Envelope tileEnvelope = new Envelope(bb.get(0), bb.get(2), bb.get(1), bb.get(3));

        final double tx = -tileEnvelope.getMinX();
        final double ty = -tileEnvelope.getMaxY();
        final double sx = (double) extent / tileEnvelope.getWidth();
        final double sy = -((double) extent / tileEnvelope.getHeight());

        GeoJSONReader reader = new GeoJSONReader();
        int i = 0;
        List<Feature> features = Arrays.stream(featureCollection.getFeatures())
                .filter(it -> it.getId().toString().compareTo("4011") > 0)
                .filter(it -> it.getId().toString().compareTo("4027") < 0)
                .collect(Collectors.toList());
        int size = features.size();
        for (Feature f : features) {
            
            i++;
            Geometry g = reader.read(f.getGeometry());
            Point centroid = g.getCentroid();
            g2d.setColor(getColor(i, size));
            g2d.drawString(f.getId().toString(), extent - 75, i * 20);
            for (int j = 0; j < g.getNumGeometries(); j++) {
                Polygon polygon = (Polygon) g.getGeometryN(j);
                Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);

                polygon.getExteriorRing().apply(new CoordinateSequenceFilter() {
                    @Override
                    public void filter(CoordinateSequence seq, int i) {

                        double _x = seq.getX(i);
                        double _y = seq.getY(i);
                        _x += tx;
                        _y += ty;
                        _x = Math.round(sx * _x);
                        _y = Math.round(sy * _y);
                        if (i == 0) {
                            path.moveTo(_x, _y);
                        } else {
                            path.lineTo(_x, _y);
                        }
                        if (i == seq.size() - 1) {
                            path.closePath();
                        }
                    }
                    @Override
                    public boolean isGeometryChanged() {
                        return false;
                    }
                    @Override
                    public boolean isDone() {
                        return false;
                    }
                });
                for (int k = 0; k < polygon.getNumInteriorRing(); k++) {
                    polygon.getInteriorRingN(k).apply(new CoordinateSequenceFilter() {
                        @Override
                        public void filter(CoordinateSequence seq, int i) {

                            double _x = seq.getX(i);
                            double _y = seq.getY(i);
                            _x += tx;
                            _y += ty;
                            _x = Math.round(sx * _x);
                            _y = Math.round(sy * _y);
                            if (i == 0) {
                                path.moveTo(_x, _y);
                            } else {
                                path.lineTo(_x, _y);
                            }
                            if (i == seq.size() - 1) {
                                path.closePath();
                            }
                        }
                        @Override
                        public boolean isGeometryChanged() {
                            return false;
                        }
                        @Override
                        public boolean isDone() {
                            return false;
                        }
                    });
                }
                g2d.fill(path);
                g2d.setColor(Color.black);
                int x = (int) Math.round((centroid.getX() + tx) * sx);
                int y = (int) Math.round((centroid.getY() + ty) * sy);
                g2d.drawString(f.getId().toString(), x, y);
            }
        }
        g2d.dispose();
        File f = new File("C:/Omat/tmp/592_json.png");
        ImageIO.write(bi, "png", f);
    }

}
