package fi.nls.hakunapi.core.param;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class DatetimeParam extends GetFeatureFilterParam {

    public static final String TAG = DatetimeParam.class.getName();

    private static final String DESCRIPTION = "Either a date-time or a period string that adheres to RFC 3339. Examples:\n\n" +
            "* A date-time: \"2018-02-12T23:20:50Z\"\n" +
            "* A period: \"2018-02-12T00:00:00Z/2018-03-18T12:31:12Z\" or \"2018-02-12T00:00:00Z/P1M6DT12H31M12S\"\n\n" +
            "Only features that have a temporal property that intersects the value of\n" +
            "`datetime` are selected.\n\n" +
            "If a feature has multiple temporal properties, it is the decision of the\n" +
            "server whether only a single temporal property is used to determine\n" +
            "the extent or all relevant temporal properties.";

    @Override
    public String getParamName() {
        return "datetime";
    }

    protected String getTag() {
        return TAG;
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new DateTimeSchema());
    }

    @Override
    public Filter toFilter(WFS3Service service, FeatureType ft, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Instant[] fromTo = parse(value, getParamName());
        if (fromTo == null) {
            return null;
        }

        List<Filter> or = new ArrayList<>();
        for (DatetimeProperty prop : ft.getDatetimeProperties()) {
            Filter f = toFilter(prop, fromTo[0], fromTo[1]);
            if (f != null) {
                or.add(f);
            }
        }
        return Filter.or(or, getTag());
    }

    public static Instant[] parse(String value, String paramName) {
        Instant from;
        Instant to;
        int i = value.indexOf('/');
        if (i < 0) {
            from = parse(value, null, paramName);
            if (from == null) {
                return null;
            }
            to = from;
        } else {
            from = parse(value.substring(0, i), null, paramName);
            to = parse(value.substring(i + 1), null, paramName);
            if (from == null && to == null) {
                return null;
            }
        }
        return new Instant[] { from, to };
    }

    private static Instant parse(String str, Instant openValue, String paramName) {
        if (str.isEmpty() || "..".equals(str)) {
            return openValue;
        }
        try {
            return Instant.parse(str);
        } catch (DateTimeParseException e) {
            String msg = String.format("'%s' value could not be parsed", paramName);
            throw new IllegalArgumentException(msg);
        }
    }

    private Filter toFilter(DatetimeProperty prop, Instant from, Instant to) {
        if (from == null) {
            if (prop.isInclusive()) {
                return Filter.lessThanThanOrEqualTo(prop.getProperty(), to.toString());
            } else {
                return Filter.lessThan(prop.getProperty(), to.toString());
            }
        } else if (to == null) {
            if (prop.isInclusive()) {
                return Filter.greaterThanOrEqualTo(prop.getProperty(), from.toString());
            } else {
                return Filter.greaterThan(prop.getProperty(), from.toString());
            }
        } else if (from.equals(to)) {
            if (prop.isInclusive()) {
                return Filter.equalTo(prop.getProperty(), from.toString());
            } else {
                return null;
            }
        } else {
            Filter _from;
            Filter _to;
            if (prop.isInclusive()) {
                _from = Filter.greaterThanOrEqualTo(prop.getProperty(), from.toString());
                _to = Filter.lessThanThanOrEqualTo(prop.getProperty(), to.toString());
            } else {
                _from = Filter.greaterThan(prop.getProperty(), from.toString());
                _to = Filter.lessThan(prop.getProperty(), to.toString());
            }
            return Filter.and(_from, _to);
        }
    }

}
