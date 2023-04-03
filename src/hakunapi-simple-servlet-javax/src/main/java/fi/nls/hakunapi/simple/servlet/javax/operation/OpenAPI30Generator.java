package fi.nls.hakunapi.simple.servlet.javax.operation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;
import fi.nls.hakunapi.core.operation.DynamicPathOperation;
import fi.nls.hakunapi.core.operation.DynamicResponseOperation;
import fi.nls.hakunapi.core.operation.OperationImpl;
import fi.nls.hakunapi.core.operation.ParametrizedOperation;
import fi.nls.hakunapi.core.param.APIParam;
import fi.nls.hakunapi.core.schemas.Component;
import fi.nls.hakunapi.core.schemas.FeaturesException;
import fi.nls.hakunapi.html.model.HTMLContext;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;

public class OpenAPI30Generator {

    private final FeatureServiceConfig service;
    private final List<OperationImpl> opToImpl;
    private final Map<String, Parameter> _parameters;
    private final Map<String, Schema> _components;

    public OpenAPI30Generator(FeatureServiceConfig service, List<OperationImpl> opToImpl) {
        this.service = service;
        this.opToImpl = opToImpl;
        this._parameters = new HashMap<>();
        this._components = new HashMap<>();
    }

    public OpenAPI create() {
        OpenAPI openApi = new OpenAPI();
        openApi.info(service.getInfo());
        openApi.servers(service.getServers());
        openApi.tags(getTags());
        openApi.paths(getPaths());
        openApi.components(new Components()
                .parameters(_parameters)
                .schemas(_components)
                .securitySchemes(service.getSecuritySchemes()));
        openApi.setSecurity(service.getSecurityRequirements());
        return openApi;
    }

    private List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        List<ApiTag> apiTags = new ArrayList<>();
        for (OperationImpl p : opToImpl) {
            ApiTag apiTag = p.operation.getTag();
            if (!apiTags.contains(apiTag)) {
                tags.add(new Tag()
                        .name(apiTag.name())
                        .description(apiTag.description));
                apiTags.add(apiTag);
            }
        }
        return tags;
    }

    private Paths getPaths() {
        Paths paths = new Paths();
        for (OperationImpl p : opToImpl) {
            Path pathBase = p.implementation.getAnnotation(Path.class);
            if (pathBase == null) {
                // LOG
                continue;
            }
            
            Map<String, Class<?>> responseClassByContentType = getResponsesByContentType(p.implementation);
            
            if (DynamicPathOperation.class.isAssignableFrom(p.implementation)) {
                DynamicPathOperation impl;
                try {
                    impl = (DynamicPathOperation) p.implementation.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                List<String> validPaths = impl.getValidPaths(service);
                for (String path : validPaths) {
                    PathItem pi = getDynamicPath(p.operation, impl, path, responseClassByContentType);
                    paths.addPathItem(path, pi);
                }
            } else {
                Method handlerMethod = getAnnotatedMethod(p.implementation, GET.class).get(0);
                String path = pathBase.value();
                if (handlerMethod.getAnnotation(Path.class) != null) {
                    path += handlerMethod.getAnnotation(Path.class).value();
                }
                List<Parameter> parameters = getParameters(handlerMethod);
                PathItem pi = getPathItem(p.operation, responseClassByContentType, parameters);
                paths.addPathItem(path, pi);
            }
        }
        return paths;
    }

    private PathItem getDynamicPath(ApiOperation op, DynamicPathOperation impl, String path, Map<String, Class<?>> responseClassByContentType) {
        PathItem pi = getPathItem(op, responseClassByContentType, null);
        for (APIParam param : impl.getParameters(path, service)) {
            Parameter parameter = param.toParameter(service);
            if (parameter == null) {
                continue;
            }
            if (param.isCommon()) {
                _parameters.put(parameter.getName(), parameter);
                Parameter ref = new Parameter();
                ref.set$ref(parameter.getName());
                pi.getGet().addParametersItem(ref);
            } else {
                pi.getGet().addParametersItem(parameter);    
            }
        }
        return pi;
    }

    private Map<String, Class<?>> getResponsesByContentType(Class<?> clazz) {
        if (DynamicResponseOperation.class.isAssignableFrom(clazz)) {
            DynamicResponseOperation impl;
            try {
                impl = (DynamicResponseOperation) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return impl.getResponsesByContentType(service);
        }

        String[] defaultContentTypes = null;
        if (clazz.getAnnotation(Produces.class) != null) {
            defaultContentTypes = clazz.getAnnotation(Produces.class).value();
        }
        Class<?>[] responseClasses = null;
        if (clazz.getAnnotation(ResponseClass.class) != null) {
            responseClasses = clazz.getAnnotation(ResponseClass.class).value();
        }
        
        Map<String, Class<?>> responsesByContentType = new HashMap<>();
        for (Method m : getAnnotatedMethod(clazz, GET.class)) {
            Class<?> responseClass = m.getReturnType();
            boolean isGenericResponse = Response.class.equals(responseClass);
            if (m.getAnnotation(Produces.class) == null) {
                for (int i = 0; i < defaultContentTypes.length; i++) {
                    String contentType = defaultContentTypes[i];
                    Class<?> cl = isGenericResponse ? responseClasses[i] : responseClass;
                    responsesByContentType.put(contentType, cl);
                }
            } else {
                String contentType = m.getAnnotation(Produces.class).value()[0];
                if (isGenericResponse) {
                    responseClass = m.getAnnotation(ResponseClass.class).value()[0];
                }
                responsesByContentType.put(contentType, responseClass);
            }
            
        }
        return responsesByContentType;
    }

    private PathItem getPathItem(ApiOperation op, Map<String, Class<?>> responsesByContentType, List<Parameter> parameters) {
        Content content = new Content();

        for (Map.Entry<String, Class<?>> kvp : responsesByContentType.entrySet()) {
            String contentType = kvp.getKey();
            Class<?> responseClass = kvp.getValue();
            Schema schema = getSchema(responseClass);
            MediaType mediaType = new MediaType().schema(schema);
            content.addMediaType(contentType, mediaType);
        }

        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", new ApiResponse()
                .description(op.get200Description())
                .content(content));
        responses._default(new ApiResponse()
                .description("An error occured")
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(getSchema(FeaturesException.class)))));

        Operation operation = new Operation()
                .summary(op.getSummary())
                .description(op.getDescription())
                .tags(Arrays.asList(op.getTag().name()))
                .responses(responses)
                .parameters(parameters);

        return new PathItem().get(operation);
    }
    
    private List<Parameter> getParameters(Method method) {
        if (method.getParameters().length == 0) {
            return null;
        }

        List<Parameter> parameterRefs = new ArrayList<>();

        boolean parametrizedClass = ParametrizedOperation.class.isAssignableFrom(method.getDeclaringClass());
        if (parametrizedClass) {
            try {
                ParametrizedOperation impl = (ParametrizedOperation) method.getDeclaringClass().newInstance();
                List<? extends APIParam> params = impl.getParameters();
                for (APIParam param : params) {
                    Parameter parameter = param.toParameter(service);
                    if (parameter == null) {
                        // This parameter doesn't want to be visible to the Api doc
                        continue;
                    }
                    _parameters.put(param.getParamName(), parameter);
                    Parameter ref = new Parameter();
                    String _ref = param.getParamName();
                    // Bypass the special handling for $refs with '.'
                    if (_ref.indexOf('.') != -1) {
                        _ref = "#/components/parameters/" + _ref;
                    }
                    ref.set$ref(_ref);
                    parameterRefs.add(ref);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (java.lang.reflect.Parameter parameter : method.getParameters()) {
            Parameter param = getParam(parameter);
            if (param != null) {
                parameterRefs.add(param);
            }
        }

        return parameterRefs;
    }

    private Parameter getParam(java.lang.reflect.Parameter parameter) {
        QueryParam qp = parameter.getAnnotation(QueryParam.class);
        PathParam pp = parameter.getAnnotation(PathParam.class);

        String name;
        if (qp != null) {
            name = qp.value();
        } else {
            if (pp != null) {
                name = pp.value();
            } else {
                return null;
            }
        }

        ParamClass paramClass = parameter.getAnnotation(ParamClass.class);
        if (paramClass != null) {
            try {
                Parameter param = _parameters.get(name);
                if (param == null) {
                    APIParam paramInstance = paramClass.value().newInstance();
                    param = paramInstance.toParameter(service);
                    _parameters.put(name, param);
                }
                Parameter ref = new Parameter();
                ref.set$ref(param.getName());
                return ref;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Probably simple schema with no descriptions
        Schema schema = getSchema(parameter.getType());
        if (qp != null) {
            return new QueryParameter()
                    .name(name)
                    .schema(schema)
                    .style(StyleEnum.FORM)
                    .explode(false);
        } else {
            return new PathParameter()
                    .name(name)
                    .schema(schema);
        }
    }

    public static List<Method> getAnnotatedMethod(Class<?> classWithMethod, Class<? extends Annotation> annotationClass) {
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : classWithMethod.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

    private Schema getSchema(Class<?> clazz) {
        if (int.class.equals(clazz) || Integer.class.equals(clazz)) {
            return new IntegerSchema();
        } else if (long.class.equals(clazz) || Long.class.equals(clazz)) {
            return new IntegerSchema().format("int64");
        } else if (float.class.equals(clazz) || Float.class.equals(clazz)) {
            return new NumberSchema();
        } else if (double.class.equals(clazz) || Double.class.equals(clazz)) {
            return new NumberSchema();
        } else if (boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
            return new BooleanSchema();
        } else if (String.class.equals(clazz)) {
            return new StringSchema();
        } else if (Date.class.equals(clazz)) {
            return new DateTimeSchema();
        } else if (Component.class.isAssignableFrom(clazz)) {
            try {
                return ((Component) clazz.getDeclaredConstructor().newInstance()).toSchema(_components);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        } else if (HTMLContext.class.equals(clazz)) {
            return new StringSchema();
            
        } else {
            throw new IllegalArgumentException("Server error");
        }
    }

}
