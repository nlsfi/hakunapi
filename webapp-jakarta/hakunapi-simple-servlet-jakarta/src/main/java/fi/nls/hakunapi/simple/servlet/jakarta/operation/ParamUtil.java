package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.ConformanceClass;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.param.BboxCrsParam;
import fi.nls.hakunapi.core.param.BboxParam;
import fi.nls.hakunapi.core.param.CollectionsParam;
import fi.nls.hakunapi.core.param.CrsParam;
import fi.nls.hakunapi.core.param.DatetimeParam;
import fi.nls.hakunapi.core.param.FParam;
import fi.nls.hakunapi.core.param.FilterCrsParam;
import fi.nls.hakunapi.core.param.FilterLangParam;
import fi.nls.hakunapi.core.param.FilterParam;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.param.LimitParam;
import fi.nls.hakunapi.core.param.NextParam;
import fi.nls.hakunapi.core.param.OffsetParam;
import fi.nls.hakunapi.core.param.PropertiesParam;
import fi.nls.hakunapi.core.param.SortbyParam;

public class ParamUtil {

    protected static final List<GetFeatureParam> ITEMS_PARAMS = Arrays.asList(
            new FParam(),
            new CollectionsParam(),
            new LimitParam(),
            new BboxParam(),
            new DatetimeParam(),
            new NextParam(),
            new SortbyParam(),
            new CrsParam(),
            new BboxCrsParam(),
            new PropertiesParam(),
            new FilterParam(),
            new FilterLangParam(),
            new FilterCrsParam()
    );
    
    static {
        ITEMS_PARAMS.sort(Comparator.comparingInt(GetFeatureParam::priority));
    }

    protected static final Map<ConformanceClass, List<GetFeatureParam>> COLLECTION_ITEMS_CONFORMANCE_PARAMS = new HashMap<>();
    static {
        COLLECTION_ITEMS_CONFORMANCE_PARAMS.put(ConformanceClass.Core,
                Arrays.asList(new FParam(), new LimitParam(), new NextParam(), new BboxParam(), new DatetimeParam(), new PropertiesParam(), new SortbyParam())); // PropertiesParam and SortbyParam aren't actually part of Core
        COLLECTION_ITEMS_CONFORMANCE_PARAMS.put(ConformanceClass.CRS,
                Arrays.asList(new CrsParam(), new BboxCrsParam()));
        COLLECTION_ITEMS_CONFORMANCE_PARAMS.put(ConformanceClass.FILTER,
                Arrays.asList(new FilterParam(), new FilterLangParam(), new FilterCrsParam()));
    }
    
    protected static final OffsetParam OFFSET_PARAM = new OffsetParam(); 

    protected static List<GetFeatureParam> getParameters(FeatureType ft, FeatureServiceConfig service) {
        List<GetFeatureParam> collectionSpecific = ft.getParameters();
        List<GetFeatureParam> params = new ArrayList<>();
        List<ConformanceClass> conformanceClasses = service.getConformanceClasses();
        if (conformanceClasses != null) {
            final List<GetFeatureParam> conformanceSpecificParams = conformanceClasses.stream()
                    .map(c -> COLLECTION_ITEMS_CONFORMANCE_PARAMS.get(c))
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream).collect(Collectors.toList());

            if (ft.getPaginationStrategy().isOffsetParamSupported()) {
                conformanceSpecificParams.add(OFFSET_PARAM);
            }

            final List<GetFeatureParam> collectionSpecificConformanceParams = ft
                    .getConformanceParams(conformanceSpecificParams);

            params.addAll(collectionSpecificConformanceParams);
        }
        params.addAll(collectionSpecific);
        params.sort(Comparator.comparingInt(GetFeatureParam::priority));
        return params;
    }

}
