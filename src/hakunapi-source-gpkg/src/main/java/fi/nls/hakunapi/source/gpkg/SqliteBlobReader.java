package fi.nls.hakunapi.source.gpkg;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sqlite.core.CoreStatement;
import org.sqlite.core.DB;
import org.sqlite.core.SafeStmtPtr;

public final class SqliteBlobReader {

    private static final Logger LOG = LoggerFactory.getLogger(SqliteBlobReader.class);

    private static final Field POINTER_FIELD;
    private static final Method COLUMN_TEXT_UTF8;

    static {
        Field pointer = null;
        Method columnText = null;
        try {
            pointer = CoreStatement.class.getDeclaredField("pointer");
            pointer.setAccessible(true);
            columnText = Class.forName("org.sqlite.core.NativeDB")
                    .getDeclaredMethod("column_text_utf8", long.class, int.class);
            columnText.setAccessible(true);
        } catch (Exception e) {
            LOG.info("Zero-copy blob reads unavailable, falling back to ResultSet#getBytes: {}",
                    e.toString());
            pointer = null;
            columnText = null;
        }
        POINTER_FIELD = pointer;
        COLUMN_TEXT_UTF8 = columnText;
    }

    private final SafeStmtPtr pointer;

    private SqliteBlobReader(SafeStmtPtr pointer) {
        this.pointer = pointer;
    }

    public static SqliteBlobReader of(PreparedStatement ps) {
        if (POINTER_FIELD == null) {
            return null;
        }
        try {
            PreparedStatement raw = ps.isWrapperFor(PreparedStatement.class)
                    ? ps.unwrap(PreparedStatement.class) : ps;
            if (!(raw instanceof CoreStatement)) {
                return null;
            }
            CoreStatement cs = (CoreStatement) raw;
            SafeStmtPtr pointer = (SafeStmtPtr) POINTER_FIELD.get(cs);
            DB db = cs.getDatabase();
            if (pointer == null || !COLUMN_TEXT_UTF8.getDeclaringClass().isInstance(db)) {
                return null;
            }
            return new SqliteBlobReader(pointer);
        } catch (Exception e) {
            LOG.info("Zero-copy blob reads unavailable for this statement: {}", e.toString());
            return null;
        }
    }

    /** The buffer is valid only until the cursor advances. */
    public ByteBuffer read(int i) {
        try {
            return pointer.safeRun((d, ptr) -> (ByteBuffer) COLUMN_TEXT_UTF8.invoke(d, ptr, i));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
