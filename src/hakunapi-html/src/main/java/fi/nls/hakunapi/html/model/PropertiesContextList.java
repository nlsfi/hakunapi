package fi.nls.hakunapi.html.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropertiesContextList implements PropertiesContext {

    private List<Object> list;
    private PropertiesContext parent;

    public PropertiesContextList(List<Object> list, PropertiesContext parent) {
        this.list = list;
        this.parent = parent;
    }

    @Override
    public PropertiesContext getParent() {
        return parent;
    }

    @Override
    public PropertiesContext addMap(String name) {
        Map<String, Object> map = new LinkedHashMap<>();
        list.add(map);
        return new PropertiesContextMap(map, this);
    }

    @Override
    public PropertiesContext addList(String name) {
        List<Object> list = new ArrayList<>();
        this.list.add(list);
        return new PropertiesContextList(list, this);
    }

    @Override
    public void add(String name, String value) {
        list.add(value);
    }

    @Override
    public void add(String name, boolean value) {
        list.add(value);
    }

    @Override
    public void add(String name, int value) {
        list.add(value);
    }

    @Override
    public void add(String name, long value) {
        list.add(value);
    }

    @Override
    public void addNullProperty(String name) {
        list.add(null);
    }

}
