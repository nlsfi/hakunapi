package fi.nls.hakunapi.proj.jhe;

import java.util.HashMap;
import java.util.Map;

import fi.nls.hakunapi.core.projection.NOPProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;

public class JHeProjectionTransformerFactory implements ProjectionTransformerFactory {

    private static final Map<String, JHeMathTransform> CACHE = new HashMap<>();

    @Override
    public ProjectionTransformer getTransformer(int sridFrom, int sridTo) throws Exception {
        JHeMathTransform t = CACHE.computeIfAbsent(getCacheKey(sridFrom, sridTo),
                __ -> JHeMathTransformFactory.findMathTransform(sridFrom, sridTo));
        if (t == null) {
            return NOPProjectionTransformer.INSTANCE;
        }
        return new JHeProjectionTransformer(sridFrom, sridTo, t);
    }

    @Override
    public ProjectionTransformer toCRS84(int sridFrom) throws Exception {
        return getTransformer(sridFrom, 4258);
    }

    @Override
    public ProjectionTransformer fromCRS84(int sridTo) throws Exception {
        return getTransformer(4258, sridTo);
    }

    private String getCacheKey(int sridFrom, int sridTo) {
        return sridFrom + "_" + sridTo;
    }

}
