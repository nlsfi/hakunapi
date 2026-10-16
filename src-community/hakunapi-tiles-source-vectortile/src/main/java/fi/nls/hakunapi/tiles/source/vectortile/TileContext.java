package fi.nls.hakunapi.tiles.source.vectortile;

public record TileContext(
        int srid,
        double minX,
        double minY,
        double maxX,
        double maxY,
        int tileWidth,
        int tileHeight,
        double resolution) {
}
