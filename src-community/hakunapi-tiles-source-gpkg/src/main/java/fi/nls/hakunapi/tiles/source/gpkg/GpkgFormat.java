package fi.nls.hakunapi.tiles.source.gpkg;

final class GpkgFormat {

    static final String PNG = "image/png";
    static final String JPEG = "image/jpeg";
    static final String WEBP = "image/webp";

    private GpkgFormat() {
    }

    static String sniff(byte[] tile) {
        if (tile == null || tile.length < 12) {
            return null;
        }
        if ((tile[0] & 0xFF) == 0x89 && tile[1] == 'P' && tile[2] == 'N' && tile[3] == 'G') {
            return PNG;
        }
        if ((tile[0] & 0xFF) == 0xFF && (tile[1] & 0xFF) == 0xD8 && (tile[2] & 0xFF) == 0xFF) {
            return JPEG;
        }
        if (tile[0] == 'R' && tile[1] == 'I' && tile[2] == 'F' && tile[3] == 'F'
                && tile[8] == 'W' && tile[9] == 'E' && tile[10] == 'B' && tile[11] == 'P') {
            return WEBP;
        }
        return null;
    }

    static boolean isGzip(byte[] tile) {
        return tile != null && tile.length >= 2 && tile[0] == (byte) 0x1f && tile[1] == (byte) 0x8b;
    }

}
