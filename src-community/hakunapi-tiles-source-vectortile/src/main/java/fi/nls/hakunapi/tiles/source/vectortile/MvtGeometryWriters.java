package fi.nls.hakunapi.tiles.source.vectortile;

import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.tiles.source.vectortile.geom.MvtGeometryEncoder;
import fi.nls.hakunapi.tiles.source.vectortile.geom.MvtLineGeometryWriter;
import fi.nls.hakunapi.tiles.source.vectortile.geom.MvtPointGeometryWriter;
import fi.nls.hakunapi.tiles.source.vectortile.geom.MvtPolygonGeometryWriter;
import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtLayerEncoder;

/** Dispatches a geometry by its WKB type to the point, line or polygon MVT writer. */
final class MvtGeometryWriters {
    private final MvtPointGeometryWriter point = new MvtPointGeometryWriter();
    private final MvtLineGeometryWriter line = new MvtLineGeometryWriter();
    private final MvtPolygonGeometryWriter polygon = new MvtPolygonGeometryWriter();

    private int mvtType;
    private MvtGeometryEncoder encoder;

    public void initTile(ProjectionTransformer proj, double minX, double maxY,
            double resolution, int tileWidth, int tileHeight, int extent, int bufferPixels) {
        point.initTile(proj, minX, maxY, resolution, tileWidth, tileHeight, extent, bufferPixels);
        line.initTile(proj, minX, maxY, resolution, tileWidth, tileHeight, extent, bufferPixels);
        polygon.initTile(proj, minX, maxY, resolution, tileWidth, tileHeight, extent, bufferPixels);
    }

    public boolean write(HakunaGeometry geometry) throws Exception {
        int wkb = geometry.getWKBType() & 0x7;
        switch (wkb) {
        case 1:
        case 4:
            point.resetFeature();
            point.writeNavigable(geometry.toNavigable());
            point.end();
            mvtType = MvtLayerEncoder.GEOM_POINT;
            encoder = point.encoder();
            return true;
        case 2:
        case 5:
            line.resetFeature();
            line.writeNavigable(geometry.toNavigable());
            mvtType = MvtLayerEncoder.GEOM_LINE;
            encoder = line.encoder();
            return true;
        case 3:
        case 6:
            polygon.resetFeature();
            polygon.writeNavigable(geometry.toNavigable());
            mvtType = MvtLayerEncoder.GEOM_POLYGON;
            encoder = polygon.encoder();
            return true;
        default:
            return false;
        }
    }

    public void trimScratch() {
        point.trimScratch();
        line.trimScratch();
        polygon.trimScratch();
    }

    public int mvtType() {
        return mvtType;
    }

    public MvtGeometryEncoder encoder() {
        return encoder;
    }
}
