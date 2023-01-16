package fi.nls.hakunapi.filter;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyBoolean;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyInt;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;

public class CQLJsonParserTest {
    
    private HakunaProperty ownerNameProp;
    private HakunaProperty floorProp;
    private HakunaProperty swimmingPoolProp;
    private HakunaProperty materialProp;
    private SimpleFeatureType ft;
    private CQLJsonParser parser = CQLJsonParser.INSTANCE;
    
    @Before
    public void before() {
        ownerNameProp = new HakunaPropertyString("owner_name", "foo", "bar", true, false, HakunaPropertyWriters.HIDDEN);
        floorProp = new HakunaPropertyInt("floor", "foo", "bar", true, false, HakunaPropertyWriters.HIDDEN);
        swimmingPoolProp = new HakunaPropertyBoolean("swimming_pool", "foo", "bar", true, false, HakunaPropertyWriters.HIDDEN);
        materialProp = new HakunaPropertyString("material", "foo", "bar", true, false, HakunaPropertyWriters.HIDDEN);
        List<HakunaProperty> queryableProperties = Arrays.asList(
                ownerNameProp,
                floorProp,
                swimmingPoolProp,
                materialProp
        );
        ft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };
        ft.setQueryableProperties(queryableProperties);
    }
    
    @Test
    public void test1() throws Exception {
        Filter filter = parser.parse(ft, read("test1.json"), 0);
        
        assertEquals(FilterOp.EQUAL_TO, filter.getOp());
        assertEquals(floorProp, filter.getProp());
        assertEquals((Integer) 5, filter.getValue());
    }
    
    @Test
    public void test5() throws Exception {
        Filter filter = parser.parse(ft, read("test5.json"), 0);
        
        assertEquals(FilterOp.NOT_LIKE, filter.getOp());
        assertEquals(ownerNameProp, filter.getProp());
        assertEquals("% Mike %", filter.getValue());
    }
    
    @Test
    public void test8() throws Exception {
        Filter filter = parser.parse(ft, read("test8.json"), 0);
        
        assertEquals(FilterOp.AND, filter.getOp());
        List<Filter> subFilters = (List<Filter>) filter.getValue();
        assertEquals(2, subFilters.size());
        
        Filter hasSwimmingPool = subFilters.get(0);
        assertEquals(FilterOp.EQUAL_TO, hasSwimmingPool.getOp());
        assertEquals(swimmingPoolProp, hasSwimmingPool.getProp());
        assertEquals(true, hasSwimmingPool.getValue());
        
        Filter or = subFilters.get(1);
        assertEquals(FilterOp.OR, or.getOp());
        
        subFilters = (List<Filter>) or.getValue();
        Filter hasMoreThan5Floors = subFilters.get(0);
        assertEquals(FilterOp.GREATER_THAN, hasMoreThan5Floors.getOp());
        assertEquals(floorProp, hasMoreThan5Floors.getProp());
        assertEquals(5, hasMoreThan5Floors.getValue());
        
        Filter materialLikeBrick = subFilters.get(1);
        assertEquals(FilterOp.LIKE, materialLikeBrick.getOp());
        assertEquals(materialProp, materialLikeBrick.getProp());
        assertEquals("%brick", materialLikeBrick.getValue());
    }
    
    private String read(String res) throws Exception {
        URI uri = getClass().getClassLoader().getResource(res).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), StandardCharsets.UTF_8);
    }

}
