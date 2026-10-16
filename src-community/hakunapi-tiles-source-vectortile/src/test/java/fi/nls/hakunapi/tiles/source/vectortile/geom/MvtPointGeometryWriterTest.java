package fi.nls.hakunapi.tiles.source.vectortile.geom;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.tiles.source.vectortile.geom.MvtCommands.Ring;

public class MvtPointGeometryWriterTest {
    private static final int EXTENT = 4096;
    private static final double TILE_SPAN = 1000.0;
    private static final double RES = TILE_SPAN / EXTENT;

    private static final GeometryFactory GF = new GeometryFactory();

    private MvtPointGeometryWriter newWriter() {
        MvtPointGeometryWriter w = new MvtPointGeometryWriter();
        w.initTile(null, 0.0, TILE_SPAN, RES, EXTENT, EXTENT, EXTENT, 0);
        return w;
    }

    private List<Ring> decode(MvtPointGeometryWriter w) {
        return MvtCommands.decode(w.encoder().commands(), w.encoder().length());
    }

    private void points(MvtPointGeometryWriter w, double... xy) throws Exception {
        org.locationtech.jts.geom.Geometry g;
        if (xy.length == 2) {
            g = GF.createPoint(new Coordinate(xy[0], xy[1]));
        } else {
            Point[] ps = new Point[xy.length / 2];
            for (int i = 0; i < ps.length; i++) {
                ps[i] = GF.createPoint(new Coordinate(xy[i * 2], xy[i * 2 + 1]));
            }
            g = GF.createMultiPoint(ps);
        }
        w.writeNavigable(new HakunaGeometryJTS(g).toNavigable());
        w.end();
    }

    @Test
    public void singlePointTransformsToTileSpace() throws Exception {
        MvtPointGeometryWriter w = newWriter();
        w.resetFeature();
        points(w, 250, 250);

        List<Ring> rs = decode(w);
        assertEquals(1, rs.size());
        int[] p = rs.get(0).pts.get(0);
        assertEquals(1024, p[0]);
        assertEquals(3072, p[1]);
    }

    @Test
    public void pointOutsideRejected() throws Exception {
        MvtPointGeometryWriter w = newWriter();
        w.resetFeature();
        points(w, 2000, 2000);
        assertEquals(0, w.encoder().length());
    }

}
