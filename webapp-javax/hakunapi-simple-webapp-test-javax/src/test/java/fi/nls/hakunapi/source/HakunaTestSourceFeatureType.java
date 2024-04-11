package fi.nls.hakunapi.source;

import java.util.List;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.SimpleFeatureType;

public class HakunaTestSourceFeatureType extends SimpleFeatureType {

	private int[] srid;
	private int storageSrid;

	// testdata
	private List<Object[]> objs;

	public int getStorageSrid() {
		return storageSrid;
	}

	public void setSrid(int[] srids) {
		this.srid = srids;

	}

	public int[] getSrid() {
		return srid;
	}

	public void setStorageSrid(int storageSrid) {
		this.storageSrid = storageSrid;
	}

	@Override
	public FeatureProducer getFeatureProducer() {
		return new HakunaTestSourceFeatureProducer(objs);
	}

	public List<Object[]> getObjs() {
		return objs;
	}

	public void setObjs(List<Object[]> objList) {
		this.objs = objList;
	}

}
