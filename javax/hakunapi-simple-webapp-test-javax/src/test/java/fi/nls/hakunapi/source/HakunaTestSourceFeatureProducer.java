package fi.nls.hakunapi.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.ObjectArrayValueContainer;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.StringPair;

public class HakunaTestSourceFeatureProducer implements FeatureProducer {

	private final List<Object[]> objs;

	public HakunaTestSourceFeatureProducer(List<Object[]> objs) {
		this.objs = objs;
	}

	@Override
	public int getNumberMatched(GetFeatureRequest request, GetFeatureCollection col) throws Exception {
		return objs.size();
	}

	protected int getColumnIndex(String column, FeatureType featureType, HakunaProperty prop) {

		if (prop instanceof HakunaPropertyGeometry) {
			return 1;
		}

		List<HakunaProperty> ftProps = featureType.getProperties();
		for (int n = 0; n < ftProps.size(); n++) {
			HakunaProperty p = ftProps.get(n);
			if ((p.getColumn() != null && column.equals(p.getColumn())) || column.equals(p.getName())) {
				return n + 2;
			}
		}
		return 0;
	}

	@Override
	public FeatureStream getFeatures(GetFeatureRequest request, GetFeatureCollection col) throws Exception {

		int offset = request.getOffset();

		QueryContext ctx = new QueryContext();
		ctx.setSRID(request.getSRID());

		Map<StringPair, Integer> columnToIndex = new HashMap<>();
		List<Integer> indexToColumn = new ArrayList<>();
		List<ValueMapper> valueMappers = new ArrayList<>();
		for (HakunaProperty property : col.getProperties()) {

			String table = property.getTable();

			if ("$id".equals(property.getName())) {
				StringPair key = new StringPair(table, null);
				columnToIndex.put(key, 0);
				indexToColumn.add(0);
				valueMappers.add(property.getMapper(columnToIndex, 0, ctx));
			} else {

				StringPair key = new StringPair(table, property.getName());
				int is = indexToColumn.size();
				columnToIndex.put(key, is);
				indexToColumn.add(getColumnIndex(property.getName(), col.getFt(), property));
				int iv = valueMappers.size();
				valueMappers.add(property.getMapper(columnToIndex, iv, ctx));
			}
		}
		int[] iToColumn = indexToColumn.stream().mapToInt(i -> i).toArray();
		HakunaTestSourceValueProvider mask = new HakunaTestSourceValueProvider(objs, iToColumn);
		ObjectArrayValueContainer container = new ObjectArrayValueContainer(valueMappers.size());

		IntStream s = scan(col.getFilters());

		if (offset > 0) {
			s = s.skip(offset);
		}
		PrimitiveIterator.OfInt rowids = s.iterator();

		return new FeatureStream() {

			@Override
			public void close() throws Exception {
				// NOP
			}

			@Override
			public boolean hasNext() {
				return rowids.hasNext();
			}

			@Override
			public ValueProvider next() {
				mask.setRow(rowids.nextInt());
				for (ValueMapper vm : valueMappers) {
					vm.accept(mask, container);
				}
				return container;
			}
		};
	}

	private IntStream scan(List<Filter> filters) throws Exception {
		if (filters.isEmpty()) {
			return IntStream.range(0, objs.size());
		}

		for (Filter filter : filters) {
			System.out.println(filter);
			switch (filter.getOp()) {
			case EQUAL_TO:

				if ("$id".equals(filter.getProp().getName())) {

					for (int o = 0; o < objs.size(); o++) {
						Object[] obj = objs.get(o);

						if (obj[0].equals(filter.getValue())) {

							return IntStream.of(o);
						}

					}

					return IntStream.empty();

				} else {
					throw new Exception("NYI - test source");

				}
			case INTERSECTS_INDEX:
				throw new Exception("NYI - test source");
			// break;
			default:
				throw new Exception("NYI - test source");

			}
		}

		throw new Exception("NYI - test source");
	}

}
