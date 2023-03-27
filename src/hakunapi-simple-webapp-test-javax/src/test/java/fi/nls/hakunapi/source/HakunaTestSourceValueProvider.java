package fi.nls.hakunapi.source;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometry;

public class HakunaTestSourceValueProvider implements ValueProvider {

	private List<Object[]> data;
	private int[] iToColumn;
	private int rowid;

	public HakunaTestSourceValueProvider(List<Object[]> data, int[] iToColumn) {
		this.data = data;
		this.iToColumn = iToColumn;
	}

	public void setRow(int rowid) {
		this.rowid = rowid;
	}

	@Override
	public boolean isNull(int i) {
		return _getObject(i) == null;
	}

	@Override
	public Boolean getBoolean(int i) {
		return (Boolean) _getObject(i);
	}

	@Override
	public Integer getInt(int i) {
		return (Integer) _getObject(i);
	}

	@Override
	public Long getLong(int i) {
		return (Long) _getObject(i);
	}

	@Override
	public Float getFloat(int i) {
		return (Float) _getObject(i);
	}

	@Override
	public Double getDouble(int i) {
		return (Double) _getObject(i);
	}

	@Override
	public Object getObject(int i) {
		return _getObject(i);
	}

	@Override
	public String getString(int i) {
		return (String) _getObject(i);
	}

	@Override
	public LocalDateTime getLocalDateTime(int i) {
		Instant instant = getInstant(i);
		if (instant == null) {
			return null;
		}
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	@Override
	public Instant getInstant(int i) {
		return (Instant) _getObject(i);
	}

	@Override
	public LocalDate getLocalDate(int i) {
		return (LocalDate) _getObject(i);
	}

	@Override
	public HakunaGeometry getHakunaGeometry(int i) {
		HakunaGeometry g = (HakunaGeometry) _getObject(i);
		return g;
	}

	@Override
	public UUID getUUID(int i) {
		return (UUID) _getObject(i);
	}

	@Override
	public int size() {
		return iToColumn.length;
	}

	@Override
	public Object[] getArray(int i) {
		return (Object[]) _getObject(i);
	}

	private Object _getObject(int i) {
		return data.get(rowid)[iToColumn[i]];
	}

}
