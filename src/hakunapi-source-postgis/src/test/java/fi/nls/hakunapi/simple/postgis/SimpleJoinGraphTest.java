package fi.nls.hakunapi.simple.postgis;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import fi.nls.hakunapi.core.join.Join;
import fi.nls.hakunapi.core.join.OneToOneJoin;
import fi.nls.hakunapi.sql.SimpleJoinGraph;

public class SimpleJoinGraphTest {
    
    @Test
    public void smokeTest() {
        Set<String> tables = new HashSet<>(Arrays.asList("abc", "def", "foo", "bar", "baz", "qux"));
        List<Join> joins = Arrays.asList(
                new OneToOneJoin("abc", null, "def", null),
                new OneToOneJoin("def", null, "foo", null),
                new OneToOneJoin("def", null, "baz", null),
                new OneToOneJoin("foo", null, "bar", null),
                new OneToOneJoin("foo", null, "baz", null),
                new OneToOneJoin("baz", null, "qux", null)
        );
        List<Join> swapped = joins.stream().map(it -> it.swapOrder()).collect(Collectors.toList());
        joins = new ArrayList<>(joins);
        joins.addAll(swapped);
        
        SimpleJoinGraph graph = new SimpleJoinGraph(tables, joins);
        List<Join> path = graph.search("abc", "qux");
        assertEquals(3, path.size());
        assertEquals("abc", path.get(0).getLeftTable());
        assertEquals("def", path.get(0).getRightTable());
        assertEquals("def", path.get(1).getLeftTable());
        assertEquals("baz", path.get(1).getRightTable());
        assertEquals("baz", path.get(2).getLeftTable());
        assertEquals("qux", path.get(2).getRightTable());
        
        path = graph.search("qux", "abc");
        assertEquals(3, path.size());
        assertEquals("qux", path.get(0).getLeftTable());
        assertEquals("baz", path.get(0).getRightTable());
        assertEquals("baz", path.get(1).getLeftTable());
        assertEquals("def", path.get(1).getRightTable());
        assertEquals("def", path.get(2).getLeftTable());
        assertEquals("abc", path.get(2).getRightTable());
    }

}
