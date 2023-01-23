package fi.nls.hakunapi.source;

import java.util.List;

import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.ObjectArrayValueContainer;
import fi.nls.hakunapi.core.ValueProvider;

public class HakunaTestSourceFeatureStream implements FeatureStream {

	public final List<Object[]> objs;
	int n = 0;

	public HakunaTestSourceFeatureStream(List<Object[]> objs) {
		this.objs = objs;
	}

	@Override
	public void close() throws Exception {
		objs.clear();
		n = 0;
	}

	@Override
	public boolean hasNext() {
		return n < objs.size();
	}

	@Override
	public ValueProvider next() {
		return ObjectArrayValueContainer.wrap(objs.get(n++));
	}

}