package fi.nls.hakunapi.core.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyInt;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;

public class FilterHelperTest {

    @Test
    public void test() {
        HakunaProperty a = new HakunaPropertyInt("foo", "foo", "foo", false, false, HakunaPropertyWriters.HIDDEN);
        HakunaProperty b = new HakunaPropertyString("bar", "bar", "bar", false, false, HakunaPropertyWriters.HIDDEN);
        HakunaProperty c = new HakunaPropertyString("baz", "baz", "baz", false, false, HakunaPropertyWriters.HIDDEN);
        HakunaProperty d = new HakunaPropertyString("qux", "qux", "qux", false, false, HakunaPropertyWriters.HIDDEN);

        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.equalTo(a, "10"));
        filters.add(Filter.and(Filter.equalTo(b, "cat"), Filter.equalTo(c, "dog")));
        filters.add(Filter.or(Filter.equalTo(b, "horse"), Filter.equalTo(d, "catdog")));

        Set<String> uniqueTables = FilterHelper.getAllProperties(filters).stream()
                .map(prop -> prop.getTable())
                .collect(Collectors.toSet());
        assertEquals(4, uniqueTables.size());
        assertTrue(uniqueTables.contains("foo"));
        assertTrue(uniqueTables.contains("bar"));
        assertTrue(uniqueTables.contains("baz"));
        assertTrue(uniqueTables.contains("qux"));
    }

}
