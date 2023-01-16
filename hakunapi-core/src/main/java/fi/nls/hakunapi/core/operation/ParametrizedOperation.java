package fi.nls.hakunapi.core.operation;

import java.util.List;

import fi.nls.hakunapi.core.param.APIParam;

public interface ParametrizedOperation {
    
    public List<? extends APIParam> getParameters();

}
