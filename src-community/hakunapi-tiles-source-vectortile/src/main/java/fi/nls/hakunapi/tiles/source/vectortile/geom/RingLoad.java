package fi.nls.hakunapi.tiles.source.vectortile.geom;

import fi.nls.hakunapi.core.projection.ProjectionTransformer;

final class RingLoad {
    static final int BBOX_LEN = 4;

    private RingLoad() {}

    static void loadUnsnapped(double[] src, int n, ProjectionTransformer proj,
            double originX, double originY, double invRes, double[] dst, double[] bbox)
            throws Exception {
        if (proj != null) {
            proj.transformInPlace(src, 0, n);
        }
        loadUnsnappedScalar(src, n, originX, originY, invRes, dst, bbox);
    }

    static void loadUnsnappedScalar(double[] src, int n, double originX, double originY,
            double invRes, double[] dst, double[] bbox) {
        if (n == 0) {
            bbox[0] = 0; bbox[1] = 0; bbox[2] = 0; bbox[3] = 0;
            return;
        }
        double px = (src[0] - originX) * invRes;
        double py = (originY - src[1]) * invRes;
        dst[0] = px;
        dst[1] = py;
        double minX = px, maxX = px;
        double minY = py, maxY = py;
        for (int i = 1; i < n; i++) {
            px = (src[i * 2] - originX) * invRes;
            py = (originY - src[i * 2 + 1]) * invRes;
            dst[i * 2] = px;
            dst[i * 2 + 1] = py;
            if (px < minX) minX = px; else if (px > maxX) maxX = px;
            if (py < minY) minY = py; else if (py > maxY) maxY = py;
        }
        bbox[0] = minX;
        bbox[1] = minY;
        bbox[2] = maxX;
        bbox[3] = maxY;
    }
}
