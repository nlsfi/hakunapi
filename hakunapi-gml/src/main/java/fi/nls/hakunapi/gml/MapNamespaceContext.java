package fi.nls.hakunapi.gml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

import fi.nls.hakunapi.core.util.SingleIterator;

public class MapNamespaceContext implements NamespaceContext {

    private final Map<String, String> prefixToURI = new HashMap<>();
    private final Map<String, String> uriToPrefix = new HashMap<>();

    public void add(String prefix, String uri) {
        prefixToURI.put(prefix, uri);
        uriToPrefix.put(uri, prefix);
    }

    public Set<Map.Entry<String, String>> getPrefixesToURIs() {
        return prefixToURI.entrySet();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return prefixToURI.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return uriToPrefix.get(namespaceURI);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return new SingleIterator<>(uriToPrefix.get(namespaceURI));
    }

}
