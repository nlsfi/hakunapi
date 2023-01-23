package fi.nls.hakunapi.filter;

import java.util.List;
import java.util.Map;

public class GeoJSONUtil {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(List<Object> list, int i) {
        return (Map<String, Object>) list.get(i);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> map, String key) {
        return (List<Object>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(List<Object> list, int i) {
        return (List<Object>) list.get(i);
    }

    public static String getString(Map<String, Object> map, String key) {
        return (String) map.get(key);
    }

    public static double getDouble(List<Object> list, int i) {
        return ((Number) list.get(i)).doubleValue();
    }

}