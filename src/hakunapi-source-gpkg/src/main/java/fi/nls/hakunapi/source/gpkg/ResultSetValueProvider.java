package fi.nls.hakunapi.source.gpkg;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.param.DatetimeParam;

public class ResultSetValueProvider implements ValueProvider {

    private final ResultSet rs;
    private final int numCols;

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
            Timestamp ts = rs.getTimestamp(i + 1);
            return ts == null ? null : ts.toInstant();
        } catch (SQLException e) {
            try {
                String str = rs.getString(i + 1);
                return DatetimeParam.RFC3339ish.parse(str, Instant::from);
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    @Override
    public LocalDateTime getLocalDateTime(int i) {
        // GpkgSimpleSource should only ever create properties of type TIMESTAMPTZ
        // which should call getInstant() instead
        try {
            return rs.getObject(i + 1, LocalDateTime.class);
        } catch (SQLException e) {
            try {
                String str = rs.getString(i + 1);
                return DatetimeParam.RFC3339ish.parse(str, LocalDateTime::from);
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    /**
     * A GeoPackage stores a DATE as {@code yyyy-mm-dd} text, so it is parsed
     * here rather than through {@code getObject(i, LocalDate.class)}.
     *
     * <p>That route throws to get its answer. sqlite-jdbc's connection date
     * format is {@code yyyy-MM-dd HH:mm:ss.SSS}, which a plain date never
     * matches, so reading one raises a {@code ParseException} and then the
     * {@code SQLException} wrapping it before the driver falls back to a format
     * that works - every time, twice per row, each with a filled-in stack trace.
     * Super inefficient for something that is eight digits and two separators.
     *
     * <p>Anything {@link #parseIsoDate} rejects still goes to the driver, so
     * datetime text and INTEGER epoch columns are unaffected. A BLOB, which used
     * to yield {@code 1970-01-01}, now throws.
     */
    @Override
    public LocalDate getLocalDate(int i) {
        try {
            byte[] utf8 = rs.getBytes(i + 1);
            if (utf8 == null) {
                return null;
            }
            LocalDate date = parseIsoDate(utf8);
            if (date != null) {
                return date;
            }
            return rs.getObject(i + 1, LocalDate.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse exactly {@code yyyy-MM-dd} from UTF-8 bytes, accepting precisely what
     * {@link LocalDate#parse} accepts, or null so the caller can fall back.
     * Bytes, so the caller can skip decoding a String per row; a date is ASCII,
     * and multi-byte UTF-8 fails the digit test and falls through.
     */
    private static LocalDate parseIsoDate(byte[] utf8) {
        if (utf8.length != 10 || utf8[4] != '-' || utf8[7] != '-') {
            return null;
        }
        int year = fourDigits(utf8, 0);
        int month = twoDigits(utf8, 5);
        int day = twoDigits(utf8, 8);
        if (year < 0 || month < 1 || month > 12 || day < 1 || day > 31) {
            return null;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            return null; // a real date shape, but not a real date (2025-02-30)
        }
    }

    /** The two ASCII digits at {@code i}, or -1 if either is not a digit. */
    private static int twoDigits(byte[] utf8, int i) {
        int hi = utf8[i] - '0';
        int lo = utf8[i + 1] - '0';
        if (hi < 0 || hi > 9 || lo < 0 || lo > 9) {
            return -1;
        }
        return hi * 10 + lo;
    }

    private static int fourDigits(byte[] utf8, int i) {
        int hi = twoDigits(utf8, i);
        int lo = twoDigits(utf8, i + 2);
        return (hi < 0 || lo < 0) ? -1 : hi * 100 + lo;
    }

    @Override
    public HakunaGeometry getHakunaGeometry(int i) {
        try {
            byte[] blob = rs.getBytes(i + 1);
            if (blob == null) {
                return null;
            }
            return new HakunaGeometryGPKG(blob);
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
