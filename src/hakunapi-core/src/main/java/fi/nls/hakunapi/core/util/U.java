package fi.nls.hakunapi.core.util;

import java.io.Closeable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class U {

    public static String firstLetterToUpperCase(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static final int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int[] parseInts(String[] s) throws NumberFormatException {
        final int[] a = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            a[i] = Integer.parseInt(s[i]);
        }
        return a;
    }

    public static long[] parseLongs(String[] s) throws NumberFormatException {
        final long[] a = new long[s.length];
        for (int i = 0; i < s.length; i++) {
            a[i] = Long.parseLong(s[i]);
        }
        return a;
    }

    public static float[] parseFloats(String[] s) throws NumberFormatException {
        final float[] a = new float[s.length];
        for (int i = 0; i < s.length; i++) {
            a[i] = Float.parseFloat(s[i]);
        }
        return a;
    }

    public static double[] parseDoubles(String[] s) throws NumberFormatException {
        final double[] a = new double[s.length];
        for (int i = 0; i < s.length; i++) {
            a[i] = Double.parseDouble(s[i]);
        }
        return a;
    }

    public static Set<String> setOf(String s, String ... s2) {
        Set<String> set = new HashSet<>();
        set.add(s);
        for (String str : s2) {
            set.add(str);
        }
        return set;
    }

    public static void closeSilent(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public static String getNameNoExtension(String path) {
        int i = path.lastIndexOf('.');
        if (i < 0) {
            return path;
        }
        return path.substring(0, i);
    }

    public static String getExtension(String path) {
        int i = path.lastIndexOf('.');
        if (i < 0 || i + 1 == path.length()) {
            return null;
        }
        return path.substring(i + 1);
    }

    public static String toQuery(Map<String, String> queryParams) {
        return toQuery(queryParams, true);
    }
    
    public static String toQuery(Map<String, String> queryParams, boolean emptyQuery) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : queryParams.entrySet()) {
            final String key = e.getKey();
            final String value = e.getValue();
            if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
                continue;
            }
            try {
                String _key = URLEncoder.encode(key, StandardCharsets.UTF_8.name()).replace("+", "%20");
                String _value = URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
                sb.append(emptyQuery ? '?' : '&');
                sb.append(_key);
                sb.append('=');
                sb.append(_value);
            } catch (Exception ignore) {
                // Trust that the URLEncoding will not fail
            }
            emptyQuery = false;
        }
        return sb.toString();
    }

    public static boolean startsWithIgnoreCase(String s, String startsWith) {
        return s.regionMatches(true, 0, startsWith, 0, startsWith.length());
    }

    public static void closeSilently(Closeable c) {
        try {
            c.close();
        } catch (Exception e) {
            // Ignore
        }
    }

}
