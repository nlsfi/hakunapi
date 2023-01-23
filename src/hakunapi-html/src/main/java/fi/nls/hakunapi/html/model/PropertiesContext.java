package fi.nls.hakunapi.html.model;

public interface PropertiesContext {

    public PropertiesContext getParent();
    public PropertiesContext addMap(String name);
    public PropertiesContext addList(String name);
    public void add(String name, String value);
    public void add(String name, boolean value);
    public void add(String name, int value);
    public void add(String name, long value);
    public void addNullProperty(String name);

}
