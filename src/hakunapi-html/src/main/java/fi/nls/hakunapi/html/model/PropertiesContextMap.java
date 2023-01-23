package fi.nls.hakunapi.html.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropertiesContextMap implements PropertiesContext {

    private Map<String, Object> map;
    private PropertiesContext parent;

    public PropertiesContextMap(Map<String, Object> map, PropertiesContext parent) {
        this.map = map;
        this.parent = parent;
    }

    @Override
    public PropertiesContext getParent() {
        return parent;
    }

    @Override
    public PropertiesContext addMap(String name) {
        Map<String, Object> map = new LinkedHashMap<>();
        this.map.put(name, map);
        return new PropertiesContextMap(map, this);
    }

    @Override
    public PropertiesContext addList(String name) {
        List<Object> list = new ArrayList<>();
        this.map.put(name, list);
        return new PropertiesContextList(list, this);
    }

    @Override
    public void add(String name, String value) {
        map.put(name, value);
    }

    @Override
    public void add(String name, boolean value) {
        map.put(name, value);
    }

    @Override
    public void add(String name, int value) {
        map.put(name, value);
    }

    @Override
    public void add(String name, long value) {
        map.put(name, value);
    }

    @Override
    public void addNullProperty(String name) {
        map.put(name, null);
    }

}
