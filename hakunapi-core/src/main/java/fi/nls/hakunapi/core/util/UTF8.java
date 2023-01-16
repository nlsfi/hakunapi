package fi.nls.hakunapi.core.util;

public class UTF8 {

    public static int getLength(CharSequence s) {
        int len = s.length();
        int utf8len = len;

        int i = 0;
        for (; i < len && s.charAt(i) < 0x80; i++) {
            // NOP
        }

        for (; i < len; i++) {
            char c = s.charAt(i);
            if (c < 0x800) {
                utf8len += ((0x7f - c) >>> 31);
            } else {
                utf8len += getLengthFallback(s, i);
                break;
            }
        }

        return utf8len;
    }

    private static int getLengthFallback(CharSequence s, int start) {
        int len = s.length();
        int utf8len = 0;

        for (int i = start; i < len; i++) {
            char c = s.charAt(i);
            if (c < 0x800) {
                utf8len += (0x7f - c) >>> 31;
            } else {
                utf8len += 2;
                if (Character.isSurrogate(c)) {
                    i++;
                }
            }
        }

        return utf8len;
    }

}
