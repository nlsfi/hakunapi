package fi.nls.hakunapi.tiles.source.vectortile.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.tiles.source.vectortile.geom.MvtCommands.Ring;

public class MvtLineGeometryWriterTest {
    private static final int EXTENT = 4096;
    private static final double TILE_SPAN = 1000.0;
    private static final double RES = TILE_SPAN / EXTENT;

    private static final GeometryFactory GF = new GeometryFactory();

    private final List<LineString> lines = new ArrayList<>();

    private MvtLineGeometryWriter newWriter() {
        MvtLineGeometryWriter w = new MvtLineGeometryWriter();
        w.initTile(null, 0.0, TILE_SPAN, RES, EXTENT, EXTENT, EXTENT, 0);
        lines.clear();
        return w;
    }

    private List<Ring> parts(MvtLineGeometryWriter w) {
        return MvtCommands.decode(w.encoder().commands(), w.encoder().length());
    }

    private void line(double... xy) {
        Coordinate[] cs = new Coordinate[xy.length / 2];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = new Coordinate(xy[i * 2], xy[i * 2 + 1]);
        }
        lines.add(GF.createLineString(cs));
    }

    private void encode(MvtLineGeometryWriter w) throws Exception {
        org.locationtech.jts.geom.Geometry g = lines.size() == 1
                ? lines.get(0)
                : GF.createMultiLineString(lines.toArray(new LineString[0]));
        w.writeNavigable(new HakunaGeometryJTS(g).toNavigable());
    }

    @Test
    public void lineExitingAndReenteringSplitsIntoTwoParts() throws Exception {
        MvtLineGeometryWriter w = newWriter();
        w.resetFeature();
        line(100, 500, 5000, 500, 100, 600);
        encode(w);

        List<Ring> ps = parts(w);
        assertEquals(2, ps.size());
        for (Ring r : ps) {
            for (int[] p : r.pts) {
                assertTrue(p[0] >= 0 && p[0] <= EXTENT);
                assertTrue(p[1] >= 0 && p[1] <= EXTENT);
            }
        }
    }

    @Test
    public void collinearMidpointsDropped() throws Exception {
        MvtLineGeometryWriter w = newWriter();
        w.resetFeature();
        line(100, 500, 500, 500, 900, 500);
        encode(w);

        List<Ring> ps = parts(w);
        assertEquals(1, ps.size());
        assertEquals(2, ps.get(0).pts.size());
    }

}
