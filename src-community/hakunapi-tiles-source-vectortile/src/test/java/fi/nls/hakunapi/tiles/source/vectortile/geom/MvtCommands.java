package fi.nls.hakunapi.tiles.source.vectortile.geom;

import java.util.ArrayList;
import java.util.List;

final class MvtCommands {
    static final class Ring {
        final List<int[]> pts = new ArrayList<>();

        double signedArea() {
            double a = 0;
            int n = pts.size();
            int prev = n - 1;
            for (int i = 0; i < n; i++) {
                int[] p1 = pts.get(prev), p2 = pts.get(i);
                a += (double) p1[0] * p2[1] - (double) p2[0] * p1[1];
                prev = i;
            }
            return a * 0.5;
        }
    }

    static List<Ring> decode(int[] cmds, int len) {
        List<Ring> rings = new ArrayList<>();
        int x = 0, y = 0;
        Ring cur = null;
        int i = 0;
        while (i < len) {
            int c = cmds[i++];
            int id = c & 0x7;
            int count = c >> 3;
            if (id == MvtGeometryEncoder.CMD_MOVETO) {
                for (int k = 0; k < count; k++) {
                    x += unzig(cmds[i++]);
                    y += unzig(cmds[i++]);
                    cur = new Ring();
                    cur.pts.add(new int[] {x, y});
                    rings.add(cur);
                }
            } else if (id == MvtGeometryEncoder.CMD_LINETO) {
                for (int k = 0; k < count; k++) {
                    x += unzig(cmds[i++]);
                    y += unzig(cmds[i++]);
                    cur.pts.add(new int[] {x, y});
                }
            } else {
            }
        }
        return rings;
    }

    private static int unzig(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private MvtCommands() {}
}
