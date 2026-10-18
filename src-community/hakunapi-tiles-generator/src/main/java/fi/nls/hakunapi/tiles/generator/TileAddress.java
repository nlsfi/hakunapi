package fi.nls.hakunapi.tiles.generator;

public record TileAddress(int zoom, long row, long col) {

    @Override
    public String toString() {
        return zoom + "/" + row + "/" + col;
    }

}
