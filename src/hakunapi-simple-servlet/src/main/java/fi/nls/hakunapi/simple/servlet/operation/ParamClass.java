package fi.nls.hakunapi.simple.servlet.operation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fi.nls.hakunapi.core.param.APIParam;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParamClass {

    Class<? extends APIParam> value();

}
