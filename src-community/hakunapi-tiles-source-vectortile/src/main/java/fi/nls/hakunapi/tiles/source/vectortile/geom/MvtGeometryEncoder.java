package fi.nls.hakunapi.tiles.source.vectortile.geom;

public final class MvtGeometryEncoder {
    public static final int CMD_MOVETO = 1;
    public static final int CMD_LINETO = 2;
    public static final int CMD_CLOSEPATH = 7;

    private int[] cmds = new int[64];
    private int len;

    private int curX;
    private int curY;

    private int originX;
    private int originY;

    public void reset() {
        len = 0;
        curX = 0;
        curY = 0;
    }

    public void setOrigin(int dx, int dy) {
        this.originX = dx;
        this.originY = dy;
    }

    public int length() {
        return len;
    }

    public int[] commands() {
        return cmds;
    }

    private void ensure(int extra) {
        int need = len + extra;
        if (need > cmds.length) {
            int cap = cmds.length;
            while (cap < need) {
                cap <<= 1;
            }
            int[] g = new int[cap];
            System.arraycopy(cmds, 0, g, 0, len);
            cmds = g;
        }
    }

    private static int command(int id, int count) {
        return (id & 0x7) | (count << 3);
    }

    private static int zigzag(int n) {
        return (n << 1) ^ (n >> 31);
    }

    public void moveTo(int x, int y) {
        ensure(3);
        cmds[len++] = command(CMD_MOVETO, 1);
        cmds[len++] = zigzag(x - curX);
        cmds[len++] = zigzag(y - curY);
        curX = x;
        curY = y;
    }

    public void closePath() {
        ensure(1);
        cmds[len++] = command(CMD_CLOSEPATH, 1);
    }

    public void moveTo(double[] xy, int i) {
        moveTo((int) xy[i * 2] - originX, (int) xy[i * 2 + 1] - originY);
    }

    public void lineTo(double[] xy, int start, int count, int step) {
        if (count <= 0) {
            return;
        }
        ensure(1 + count * 2);
        cmds[len++] = command(CMD_LINETO, count);
        int idx = start;
        for (int i = 0; i < count; i++) {
            int x = (int) xy[idx * 2] - originX;
            int y = (int) xy[idx * 2 + 1] - originY;
            cmds[len++] = zigzag(x - curX);
            cmds[len++] = zigzag(y - curY);
            curX = x;
            curY = y;
            idx += step;
        }
    }

    public void moveToRun(double[] xy, int count) {
        if (count <= 0) {
            return;
        }
        ensure(1 + count * 2);
        cmds[len++] = command(CMD_MOVETO, count);
        for (int i = 0; i < count; i++) {
            int x = (int) xy[i * 2] - originX;
            int y = (int) xy[i * 2 + 1] - originY;
            cmds[len++] = zigzag(x - curX);
            cmds[len++] = zigzag(y - curY);
            curX = x;
            curY = y;
        }
    }

    public void ring(double[] xy, int start, int m, boolean reverse) {
        if (m < 3) {
            return;
        }
        if (!reverse) {
            moveTo(xy, start);
            lineTo(xy, start + 1, m - 1, +1);
        } else {
            int first = start + m - 1;
            moveTo(xy, first);
            lineTo(xy, first - 1, m - 1, -1);
        }
        closePath();
    }
}
