package fi.nls.hakunapi.simple.servlet.operation.param;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyTimestamptz;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class TimestampParam implements CustomizableGetFeatureParam {

	private FeatureType ft;
	private HakunaProperty start;
	private Optional<HakunaProperty> endOpt;
	private String name;

	@Override
	public void init(HakunaProperty prop, String name, String desc, String arg) {
		this.name = name;
		this.ft = prop.getFeatureType();
		this.start = (HakunaPropertyTimestamptz) prop;
		this.endOpt = ft.getProperties().stream().filter(p -> p.getName().equals(arg)).findFirst();
	}

	@Override
	public String getParamName() {
		return this.name;
	}

	public String getDescription() {
		return "Filter the collection by " + getParamName();
	}

	@Override
	public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {

		if( value == null||value.isEmpty()) {
			return;
		}
		if (endOpt.isPresent()) {
			final HakunaProperty end = endOpt.get();
			/* iff there's a separate end property */
			request.getCollections().stream().filter(c -> c.getFt().getName().equals(ft.getName())).findAny()
					.ifPresent(c -> {

						List<Filter> filters = Arrays.asList(
								Filter.and(Filter.isNotNull(start), Filter.lessThanThanOrEqualTo(start, value)),
								Filter.or(Filter.isNull(end), Filter.greaterThan(end, value)));
						c.addFilter(Filter.and(filters));
					});

		} else {
			request.getCollections().stream().filter(c -> c.getFt().getName().equals(ft.getName())).findAny()
					.ifPresent(c -> {

						List<Filter> filters = Arrays.asList(Filter.isNotNull(start), Filter.equalTo(start, value));

						c.addFilter(Filter.and(filters));
					});

		}

	}

	@Override
	public Parameter toParameter(WFS3Service service) {
		return new QueryParameter().name(getParamName()).style(StyleEnum.FORM).explode(false).required(false)
				.description(getDescription());
	}

}
