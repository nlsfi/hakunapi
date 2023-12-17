package fi.nls.hakunapi.sql;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryEWKB;

public class ResultSetValueProvider implements ValueProvider {

    protected final ResultSet rs;
    protected final int numCols;

    public ResultSetValueProvider(ResultSet rs, int numCols) {
        this.rs = rs;
        this.numCols = numCols;
    }

    @Override
    public boolean isNull(int i) {
        try {
            return rs.getObject(i + 1) == null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean getBoolean(int i) {
        try {
            boolean b = rs.getBoolean(i + 1);
            return !b && rs.wasNull() ? null : b;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer getInt(int i) {
        try {
            int v = rs.getInt(i + 1);
            return v == 0 && rs.wasNull() ? null : v;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long getLong(int i) {
        try {
            long v = rs.getLong(i + 1);
            return v == 0 && rs.wasNull() ? null : v;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Float getFloat(int i) {
        try {
            float v = rs.getFloat(i + 1);
            return v == 0 && rs.wasNull() ? null : v;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Double getDouble(int i) {
        try {
            double v = rs.getDouble(i + 1);
            return v == 0 && rs.wasNull() ? null : v;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getObject(int i) {
        try {
            return rs.getObject(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getString(int i) {
        try {
            return rs.getString(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Instant getInstant(int i) {
        try {
            OffsetDateTime odt = rs.getObject(i + 1, OffsetDateTime.class);
            return odt == null ? null : odt.toInstant();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LocalDateTime getLocalDateTime(int i) {
        try {
            return rs.getObject(i + 1, LocalDateTime.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LocalDate getLocalDate(int i) {
        try {
            return rs.getObject(i + 1, LocalDate.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HakunaGeometry getHakunaGeometry(int i) {
        try {
            byte[] ewkb = rs.getBytes(i + 1);
            if (ewkb == null) {
                return null;
            }
            return new HakunaGeometryEWKB(ewkb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UUID getUUID(int i) {
        try {
            return rs.getObject(i + 1, UUID.class);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public int size() {
        return numCols;
    }

    @Override
    public Object[] getArray(int i) {
        try {
            Array array = rs.getArray(i + 1);
            if (array == null) {
                return null;
            }
            Object actual = array.getArray();
            array.free();
            return (Object[]) actual;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public byte[] getJSON(int i) {
        try {
            String json = rs.getString(i + 1);
            if (json == null) {
                return null;
            }
            byte[] actual = json.getBytes(StandardCharsets.UTF_8);
            return actual;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
