package fi.nls.hakunapi.tiles.source.vectortile.geom;

import fi.nls.hakunapi.core.geom.NavigableHakunaGeometry;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;

public abstract class AbstractMvtGeometryWriter {
    private static final int INITIAL_COORDS = 256;

    private static final int RETAINED_COORDS = 16384;

    protected ProjectionTransformer proj;

    protected double originX;
    protected double originY;
    protected double invRes;

    protected double clipMinX;
    protected double clipMinY;
    protected double clipMaxX;
    protected double clipMaxY;

    protected double[] world;

    protected double[] scratch;
    protected int n;

    protected double[] scratchB;
    protected double[] scratchC;

    protected double ringMinX, ringMinY, ringMaxX, ringMaxY;
    protected boolean ringBboxValid;

    private final double[] bbox = new double[RingLoad.BBOX_LEN];

    protected AbstractMvtGeometryWriter() {
        this.world = new double[INITIAL_COORDS * 2];
        this.scratch = new double[INITIAL_COORDS * 2];
        this.scratchB = new double[INITIAL_COORDS * 2];
        this.scratchC = new double[INITIAL_COORDS * 2];
    }

    public void initTile(ProjectionTransformer proj, double minX, double maxY,
            double resolution, int tileWidth, int tileHeight, int extent, int bufferPixels) {
        this.proj = proj;
        this.originX = minX;
        this.originY = maxY;
        this.invRes = extent / (tileWidth * resolution);
        this.clipMinX = -bufferPixels;
        this.clipMinY = -bufferPixels;
        this.clipMaxX = (double) extent + bufferPixels;
        this.clipMaxY = (double) extent + bufferPixels;
    }

    public void trimScratch() {
        int cap = RETAINED_COORDS * 2;
        if (world.length > cap) {
            world = new double[cap];
        }
        if (scratch.length > cap) {
            scratch = new double[cap];
        }
        if (scratchB.length > cap) {
            scratchB = new double[cap];
        }
        if (scratchC.length > cap) {
            scratchC = new double[cap];
        }
    }

    protected final double toTileX(double worldX) {
        return (worldX - originX) * invRes;
    }

    protected final double toTileY(double worldY) {
        return (originY - worldY) * invRes;
    }

    protected final void resetRing() {
        n = 0;
        ringBboxValid = false;
    }

    protected final void loadRing(NavigableHakunaGeometry part, int r) throws Exception {
        int sz = part.getRingSize(r);
        if (sz * 2 > world.length) {
            world = grow(world, sz * 2);
        }
        if (sz * 2 > scratch.length) {
            scratch = grow(scratch, sz * 2);
        }
        part.copyRingXY(r, world, 0, 0, sz);
        RingLoad.loadUnsnapped(world, sz, proj, originX, originY, invRes, scratch, bbox);
        n = sz;
        ringMinX = bbox[0];
        ringMinY = bbox[1];
        ringMaxX = bbox[2];
        ringMaxY = bbox[3];
        ringBboxValid = true;
    }

    protected static double[] grow(double[] a, int need) {
        return new double[capacity(a.length, need)];
    }

    protected static double[] growKeeping(double[] a, int need) {
        double[] b = new double[capacity(a.length, need)];
        System.arraycopy(a, 0, b, 0, a.length);
        return b;
    }

    private static int capacity(int len, int need) {
        while (len < need) {
            len <<= 1;
        }
        return len;
    }

    protected final void ensureB(int pairs) {
        if (pairs * 2 > scratchB.length) {
            scratchB = grow(scratchB, pairs * 2);
        }
        if (pairs * 2 > scratchC.length) {
            scratchC = grow(scratchC, pairs * 2);
        }
    }
}
