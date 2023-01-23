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
import org.locationtech.jts.geom.Polygon;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyBoolean;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyInt;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;

public class JsonFilterExprParserTest {
    
    private HakunaProperty beamModeProp;
    private HakunaProperty swathDirectionProp;
    private HakunaProperty polarizationProp;
    private HakunaProperty ownerProp;
    private HakunaProperty floorProp;
    private HakunaProperty swimmingPoolProp;
    private HakunaProperty materialProp;
    private SimpleFeatureType ft;
    private FilterParser parser;

    @Before
    public void before() {
        boolean nullable = true;
        boolean unique = false;
        beamModeProp = new HakunaPropertyString("beamMode", "foo", "bar", nullable, unique, HakunaPropertyWriters.HIDDEN);
        swathDirectionProp = new HakunaPropertyString("swathDirection", "foo", "bar", nullable, unique, HakunaPropertyWriters.HIDDEN);
        polarizationProp = new HakunaPropertyString("polarization", "foo", "bar", nullable, unique, HakunaPropertyWriters.HIDDEN);
        ownerProp = new HakunaPropertyString("owner", "foo", "bar", nullable, unique, HakunaPropertyWriters.HIDDEN);
        floorProp = new HakunaPropertyInt("floors", "foo", "bar", nullable, unique, HakunaPropertyWriters.HIDDEN);
        swimmingPoolProp = new HakunaPropertyBoolean("swimming_pool", "foo", "bar", nullable, unique, HakunaPropertyWriters.HIDDEN);
        materialProp = new HakunaPropertyString("material", "foo", "bar", nullable, unique, HakunaPropertyWriters.HIDDEN);
        List<HakunaProperty> queryableProperties = Arrays.asList(
                beamModeProp,
                swathDirectionProp,
                polarizationProp,
                ownerProp,
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
        parser = JsonFilterExprParser.INSTANCE;
    }
    
    @Test
    public void test0() throws Exception {
        Filter filter = parser.parse(ft, read("jfe0.json"), 0);
        
        assertEquals(FilterOp.AND, filter.getOp());
        List<Filter> subFilters = (List<Filter>) filter.getValue();
        assertEquals(4, subFilters.size());
        
        Filter beam = subFilters.get(0);
        assertEquals(FilterOp.EQUAL_TO, beam.getOp());
        assertEquals(beamModeProp, beam.getProp());
        assertEquals("ScanSAR Narrow", beam.getValue());
        
        Filter swath = subFilters.get(1);
        assertEquals(FilterOp.EQUAL_TO, swath.getOp());
        assertEquals(swathDirectionProp, swath.getProp());
        assertEquals("ascending", swath.getValue());
        
        Filter polarization = subFilters.get(2);
        assertEquals(FilterOp.EQUAL_TO, polarization.getOp());
        assertEquals(polarizationProp, polarization.getProp());
        assertEquals("HH+VV+HV+VH", polarization.getValue());
        
        Filter intersects = subFilters.get(3);
        assertEquals(FilterOp.INTERSECTS, intersects.getOp());
        assertEquals(ft.getGeom(), intersects.getProp());
        assertEquals(Polygon.class, intersects.getValue().getClass());
    }
    
    @Test
    public void test1() throws Exception {
        Filter filter = parser.parse(ft, read("jfe1.json"), 0);
        
        assertEquals(FilterOp.GREATER_THAN, filter.getOp());
        assertEquals(floorProp, filter.getProp());
        assertEquals((Integer) 5, filter.getValue());
    }
    
    @Test
    public void test5() throws Exception {
        Filter filter = parser.parse(ft, read("jfe5.json"), 0);
        
        assertEquals(FilterOp.NOT_LIKE, filter.getOp());
        assertEquals(ownerProp, filter.getProp());
        assertEquals("% Mike %", filter.getValue());
    }
    
    @Test
    public void test8() throws Exception {
        Filter filter = parser.parse(ft, read("jfe8.json"), 0);
        
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
