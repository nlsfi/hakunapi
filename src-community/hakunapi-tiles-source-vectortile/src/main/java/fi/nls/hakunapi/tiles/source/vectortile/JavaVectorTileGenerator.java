package fi.nls.hakunapi.tiles.source.vectortile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.Bbox;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.projection.ProjectionHelper;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.tiles.TileBody;
import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtLayerEncoder;
import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtTileWriter;

/** Encodes Mapbox Vector Tiles in-JVM from hakunapi feature producers, one layer per collection. */
public class JavaVectorTileGenerator implements VectorTileGenerator {
    public static final String TYPE = "hakunapi";

    private static final int MVT_EXTENT = 4096;

    private static final int DEFAULT_BUFFER = 256;

    private final ThreadLocal<MvtGeometryWriters> writers =
            ThreadLocal.withInitial(MvtGeometryWriters::new);

    private final ThreadLocal<MvtLayerEncoder> encoders =
            ThreadLocal.withInitial(MvtLayerEncoder::new);

    private final ThreadLocal<double[]> envelopes =
            ThreadLocal.withInitial(() -> new double[4]);

    private final ThreadLocal<List<MvtGeometryWriters>> blockWriters =
            ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<MvtLayerEncoder>> blockEncoders =
            ThreadLocal.withInitial(ArrayList::new);

    private ProjectionTransformer tileProjection(FeatureType ft, int tileSrid) throws Exception {
        HakunaPropertyGeometry geom = ft.getGeom();
        if (geom == null || geom.getStorageSRID() == tileSrid) {
            return null;
        }
        ProjectionTransformer t = ft.getProjectionTransformerFactory()
                .getTransformer(geom.getStorageSRID(), tileSrid);
        return t.isNOP() ? null : t;
    }

    private int querySrid(FeatureType ft, int tileSrid) throws Exception {
        return tileProjection(ft, tileSrid) != null
                ? ft.getGeom().getStorageSRID() : tileSrid;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Optional<TileBody> generate(TileContext ctx, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) throws Exception {
        if (collections.isEmpty()) {
            return Optional.of(out -> {  });
        }

        return Optional.of(out -> {
            MvtGeometryWriters writers = this.writers.get();
            MvtLayerEncoder layer = encoders.get();
            double margin = DEFAULT_BUFFER * ctx.tileWidth() * ctx.resolution() / MVT_EXTENT;
            double[] clipBox = { ctx.minX() - margin, ctx.minY() - margin,
                    ctx.maxX() + margin, ctx.maxY() + margin };
            double[] envelope = envelopes.get();
            try {
                for (int i = 0; i < collections.size(); i++) {
                    encodeLayer(ctx, collections.get(i), writers, layer, clipBox, envelope);
                    if (layer.getFeatureCount() == 0) {
                        continue;
                    }
                    layer.finish();
                    MvtTileWriter.writeLayer(out, layer);
                }
            } finally {
                writers.trimScratch();
                layer.trim();
            }
        });
    }

    @Override
    public boolean supportsBlocks() {
        return true;
    }

    @Override
    public List<byte[]> generateBlock(List<TileContext> ctxs, List<FeatureType> collections,
            double[] queryBox) throws Exception {
        int tiles = ctxs.size();
        List<MvtLayerEncoder> encoders = blockEncoders.get();
        while (encoders.size() < tiles) {
            encoders.add(new MvtLayerEncoder());
        }
        List<MvtGeometryWriters> writers = blockWriters.get();
        while (writers.size() < tiles) {
            writers.add(new MvtGeometryWriters());
        }

        List<ByteArrayOutputStream> out = new ArrayList<>(tiles);
        double[] clipBoxes = new double[tiles * 4];
        for (int t = 0; t < tiles; t++) {
            TileContext ctx = ctxs.get(t);
            double margin = clipMargin(ctx);
            clipBoxes[t * 4] = ctx.minX() - margin;
            clipBoxes[t * 4 + 1] = ctx.minY() - margin;
            clipBoxes[t * 4 + 2] = ctx.maxX() + margin;
            clipBoxes[t * 4 + 3] = ctx.maxY() + margin;
            out.add(new ByteArrayOutputStream());
        }

        double[] envelope = envelopes.get();
        try {
            for (int i = 0; i < collections.size(); i++) {
                encodeBlockLayer(ctxs, collections.get(i), writers, encoders,
                        clipBoxes, envelope, queryBox, out);
            }
        } finally {
            for (int t = 0; t < tiles; t++) {
                writers.get(t).trimScratch();
                encoders.get(t).trim();
            }
        }

        List<byte[]> result = new ArrayList<>(tiles);
        for (int t = 0; t < tiles; t++) {
            result.add(out.get(t).toByteArray());
        }
        return result;
    }

    private void encodeBlockLayer(List<TileContext> ctxs, FeatureType ft,
            List<MvtGeometryWriters> writers, List<MvtLayerEncoder> encoders,
            double[] clipBoxes, double[] envelope, double[] queryBox,
            List<ByteArrayOutputStream> out) throws Exception {
        int tiles = ctxs.size();
        for (int t = 0; t < tiles; t++) {
            encoders.get(t).reset(ft.getName());
            encoders.get(t).setExtent(MVT_EXTENT);
        }

        HakunaPropertyGeometry geomProp = ft.getGeom();
        if (geomProp == null) {
            return;
        }

        ProjectionTransformer proj = tileProjection(ft, ctxs.get(0).srid());
        for (int t = 0; t < tiles; t++) {
            TileContext ctx = ctxs.get(t);
            writers.get(t).initTile(proj, ctx.minX(), ctx.maxY(), ctx.resolution(),
                    ctx.tileWidth(), ctx.tileHeight(), MVT_EXTENT, DEFAULT_BUFFER);
        }

        GetFeatureRequest request = new GetFeatureRequest();
        request.setSRID(querySrid(ft, ctxs.get(0).srid()));
        request.setBboxSrid(ctxs.get(0).srid());
        request.setLimit(fi.nls.hakunapi.core.param.LimitParam.UNLIMITED);

        GetFeatureCollection col = new GetFeatureCollection(ft);
        addBboxFilter(ctxs.get(0).srid(), geomProp, col, queryBox);
        request.addCollection(col);

        List<HakunaProperty> props = col.getProperties();
        HakunaProperty idProp = ft.getId();
        int geomIdx = props.indexOf(geomProp);
        int idIdx = props.indexOf(idProp);
        HakunaPropertyType idType = null;
        if (idProp != null && (idProp.getType() == HakunaPropertyType.INT
                || idProp.getType() == HakunaPropertyType.LONG)) {
            idType = idProp.getType();
        }

        int n = props.size();
        int[][] keyIdxByProp = new int[tiles][n];
        for (int t = 0; t < tiles; t++) {
            MvtLayerEncoder layer = encoders.get(t);
            for (int i = 0; i < n; i++) {
                HakunaProperty p = props.get(i);
                keyIdxByProp[t][i] = (p == geomProp || p == idProp)
                        ? -1 : layer.keyIndex(p.getName());
            }
        }

        try (FeatureStream stream = ft.getFeatureProducer().getFeatures(request, col)) {
            while (stream.hasNext()) {
                ValueProvider vp = stream.next();
                for (int t = 0; t < tiles; t++) {
                    encodeFeature(vp, props, geomIdx, idIdx, idType, keyIdxByProp[t],
                            writers.get(t), encoders.get(t), clipBoxes, t * 4, envelope);
                }
            }
        }

        for (int t = 0; t < tiles; t++) {
            MvtLayerEncoder layer = encoders.get(t);
            if (layer.getFeatureCount() == 0) {
                continue;
            }
            layer.finish();
            MvtTileWriter.writeLayer(out.get(t), layer);
        }
    }

    private void encodeLayer(TileContext ctx, FeatureType ft,
            MvtGeometryWriters writers, MvtLayerEncoder layer,
            double[] clipBox, double[] envelope) throws Exception {
        layer.reset(ft.getName());
        layer.setExtent(MVT_EXTENT);

        HakunaPropertyGeometry geomProp = ft.getGeom();
        if (geomProp == null) {
            return;
        }
        ProjectionTransformer proj = tileProjection(ft, ctx.srid());
        writers.initTile(proj, ctx.minX(), ctx.maxY(), ctx.resolution(),
                ctx.tileWidth(), ctx.tileHeight(), MVT_EXTENT, DEFAULT_BUFFER);

        GetFeatureRequest request = new GetFeatureRequest();
        request.setSRID(querySrid(ft, ctx.srid()));
        request.setBboxSrid(ctx.srid());
        request.setLimit(fi.nls.hakunapi.core.param.LimitParam.UNLIMITED);

        GetFeatureCollection col = new GetFeatureCollection(ft);
        addBboxFilter(ctx, geomProp, col);
        request.addCollection(col);

        List<HakunaProperty> props = col.getProperties();
        HakunaProperty idProp = ft.getId();
        int geomIdx = props.indexOf(geomProp);

        int n = props.size();
        int[] keyIdxByProp = new int[n];
        for (int i = 0; i < n; i++) {
            HakunaProperty p = props.get(i);
            if (p == geomProp || p == idProp) {
                keyIdxByProp[i] = -1;
            } else {
                keyIdxByProp[i] = layer.keyIndex(p.getName());
            }
        }
        int idIdx = props.indexOf(idProp);
        HakunaPropertyType idType = null;
        if (idProp != null && (idProp.getType() == HakunaPropertyType.INT
                || idProp.getType() == HakunaPropertyType.LONG)) {
            idType = idProp.getType();
        }

        try (FeatureStream stream = ft.getFeatureProducer().getFeatures(request, col)) {
            while (stream.hasNext()) {
                ValueProvider vp = stream.next();
                encodeFeature(vp, props, geomIdx, idIdx, idType, keyIdxByProp, writers, layer,
                        clipBox, 0, envelope);
            }
        }
    }

    private void encodeFeature(ValueProvider vp, List<HakunaProperty> props,
            int geomIdx, int idIdx, HakunaPropertyType idType, int[] keyIdxByProp,
            MvtGeometryWriters writers, MvtLayerEncoder layer,
            double[] clipBoxes, int clipOff, double[] envelope) throws Exception {
        if (geomIdx < 0 || vp.isNull(geomIdx)) {
            return;
        }
        HakunaGeometry geom = vp.getHakunaGeometry(geomIdx);
        if (geom == null) {
            return;
        }
        if (clipOff >= 0 && geom.readEnvelopeIfCheap(envelope)
                && (envelope[2] < clipBoxes[clipOff] || envelope[0] > clipBoxes[clipOff + 2]
                 || envelope[3] < clipBoxes[clipOff + 1] || envelope[1] > clipBoxes[clipOff + 3])) {
            return;
        }
        if (!writers.write(geom) || writers.encoder().length() == 0) {
            return;
        }

        long id = (idType != null && idIdx >= 0 && !vp.isNull(idIdx))
                ? idOf(vp, idIdx, idType) : 0;

        layer.beginFeature();
        for (int i = 0; i < props.size(); i++) {
            int keyIdx = keyIdxByProp[i];
            if (keyIdx < 0 || vp.isNull(i)) {
                continue;
            }
            tagAttribute(layer, keyIdx, props.get(i).getType(), vp, i);
        }
        layer.endFeature(id, writers.mvtType(),
                writers.encoder().commands(), writers.encoder().length());
    }

    private static long idOf(ValueProvider vp, int i, HakunaPropertyType type) {
        if (type == HakunaPropertyType.LONG) {
            Long l = vp.getLong(i);
            return l != null ? l.longValue() : 0;
        }
        Integer n = vp.getInt(i);
        return n != null ? n.longValue() : 0;
    }

    private static void tagAttribute(MvtLayerEncoder layer, int keyIdx,
            HakunaPropertyType type, ValueProvider vp, int i) {
        switch (type) {
        case STRING:
            String s = vp.getString(i);
            if (s != null) layer.tagString(keyIdx, s);
            break;
        case BOOLEAN:
            Boolean b = vp.getBoolean(i);
            if (b != null) layer.tagBool(keyIdx, b);
            break;
        case INT:
            Integer n = vp.getInt(i);
            if (n != null) layer.tagLong(keyIdx, n.longValue());
            break;
        case LONG:
            Long l = vp.getLong(i);
            if (l != null) layer.tagLong(keyIdx, l.longValue());
            break;
        case FLOAT:
            Float f = vp.getFloat(i);
            if (f != null) layer.tagFloat(keyIdx, f.floatValue());
            break;
        case DOUBLE:
            Double d = vp.getDouble(i);
            if (d != null) layer.tagDouble(keyIdx, d.doubleValue());
            break;
        case DATE:
            LocalDate date = vp.getLocalDate(i);
            if (date != null) layer.tagDate(keyIdx, date);
            break;
        default:
            Object o = vp.getObject(i);
            if (o != null) layer.tagString(keyIdx, o.toString());
        }
    }

    public static double clipMargin(TileContext ctx) {
        return DEFAULT_BUFFER * ctx.tileWidth() * ctx.resolution() / MVT_EXTENT;
    }

    private static void addBboxFilter(TileContext ctx, HakunaPropertyGeometry geomProp,
            GetFeatureCollection col) {
        double margin = clipMargin(ctx);
        addBboxFilter(ctx.srid(), geomProp, col,
                new double[] { ctx.minX() - margin, ctx.minY() - margin,
                        ctx.maxX() + margin, ctx.maxY() + margin });
    }

    private static void addBboxFilter(int srid, HakunaPropertyGeometry geomProp,
            GetFeatureCollection col, double[] box) {
        Envelope env = new Envelope(box[0], box[2], box[1], box[3]);
        Geometry bbox = Bbox.toGeometry(env);
        bbox.setSRID(srid);
        Geometry storage = ProjectionHelper.reprojectToStorageCRS(geomProp, bbox);
        col.addFilter(Filter.intersectsIndex(geomProp, storage));
    }
}
