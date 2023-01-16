package fi.nls.hakunapi.simple.servlet.operation;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import fi.nls.hakunapi.core.ConformanceClass;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.param.GetFeatureParam;

public class ParamUtilTest {

    WFS3Service service() {
        return new WFS3Service() {

            @Override
            public Collection<FeatureType> getCollections() {
                return null;
            }

            @Override
            public FeatureType getCollection(String name) {
                return null;
            }

            @Override
            public OutputFormat getOutputFormat(String f) {
                return null;
            }

            @Override
            public Collection<OutputFormat> getOutputFormats() {
                return null;
            }

        };
    }

    @Test
    public void testEmptyConformanceParams() {

        final WFS3Service service = service();
        final SimpleFeatureType sft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };
        List<GetFeatureParam> params = ParamUtil.getParameters(sft, service);

        assertTrue(params.isEmpty());
    }

    @Test
    public void testCoreConformanceParams() {
        final List<ConformanceClass> conformsTo = new ArrayList<>();
        conformsTo.add(ConformanceClass.Core);
        conformsTo.add(ConformanceClass.OpenAPI30);
        conformsTo.add(ConformanceClass.GeoJSON);
        conformsTo.add(ConformanceClass.HTML);

        final WFS3Service service = service();
        service.setConformanceClasses(conformsTo);

        final SimpleFeatureType sft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };

        List<GetFeatureParam> expected = ParamUtil.COLLECTION_ITEMS_CONFORMANCE_PARAMS.get(ConformanceClass.Core);
        List<GetFeatureParam> params = ParamUtil.getParameters(sft, service);

        assertTrue(expected.size() == params.size());
        for (int j = 0; j < expected.size(); j++) {
            assertTrue(expected.get(j).equals(params.get(j)));
        }
    }

    @Test
    public void testCoreCrsConformanceParams() {
        final List<ConformanceClass> conformsTo = new ArrayList<>();
        conformsTo.add(ConformanceClass.Core);
        conformsTo.add(ConformanceClass.OpenAPI30);
        conformsTo.add(ConformanceClass.GeoJSON);
        conformsTo.add(ConformanceClass.HTML);
        conformsTo.add(ConformanceClass.CRS);

        final WFS3Service service = service();
        service.setConformanceClasses(conformsTo);

        final SimpleFeatureType sft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };

        List<GetFeatureParam> expected = new ArrayList<>();
        expected.addAll(ParamUtil.COLLECTION_ITEMS_CONFORMANCE_PARAMS.get(ConformanceClass.Core));
        expected.addAll(ParamUtil.COLLECTION_ITEMS_CONFORMANCE_PARAMS.get(ConformanceClass.CRS));
        List<GetFeatureParam> params = ParamUtil.getParameters(sft, service);

        assertTrue(expected.size() == params.size());
        for (int j = 0; j < expected.size(); j++) {
            assertTrue(expected.get(j).equals(params.get(j)));
        }
    }

    @Test
    public void testCoreCrsFilterConformanceParams() {
        final List<ConformanceClass> conformsTo = new ArrayList<>();
        conformsTo.add(ConformanceClass.Core);
        conformsTo.add(ConformanceClass.OpenAPI30);
        conformsTo.add(ConformanceClass.GeoJSON);
        conformsTo.add(ConformanceClass.HTML);
        conformsTo.add(ConformanceClass.CRS);
        conformsTo.add(ConformanceClass.FILTER);
        conformsTo.add(ConformanceClass.FEATURES_FILTER);

        final WFS3Service service = service();
        service.setConformanceClasses(conformsTo);

        final SimpleFeatureType sft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };

        List<GetFeatureParam> expected = new ArrayList<>();
        expected.addAll(ParamUtil.COLLECTION_ITEMS_CONFORMANCE_PARAMS.get(ConformanceClass.Core));
        expected.addAll(ParamUtil.COLLECTION_ITEMS_CONFORMANCE_PARAMS.get(ConformanceClass.CRS));
        expected.addAll(ParamUtil.COLLECTION_ITEMS_CONFORMANCE_PARAMS.get(ConformanceClass.FILTER));
        List<GetFeatureParam> params = ParamUtil.getParameters(sft, service);

        assertTrue(expected.size() == params.size());
        for (GetFeatureParam e : expected) {
            assertTrue(params.contains(e));
        }
    }

}
