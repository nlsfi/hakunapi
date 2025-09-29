package fi.nls.hakunapi.geojson.hakuna;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.io.NumberOutput;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.util.UTF8;

public class HakunaJsonWriter implements AutoCloseable, Flushable {

    private static final int BUF_LEN = 8192;

    private static final byte QUOTE = '"';
    private static final byte COLON = ':';
    private static final byte COMMA = ',';
    private static final byte START_OBJ = '{';
    private static final byte END_OBJ = '}';
    private static final byte START_ARR = '[';
    private static final byte END_ARR = ']';

    private static final byte[] NULL = { 'n', 'u', 'l', 'l' };
    private static final byte[] TRUE = { 't', 'r', 'u', 'e' };
    private static final byte[] FALSE = { 'f', 'a', 'l', 's', 'e' };

    private static final byte[] ESCAPE;
    static {
        ESCAPE = new byte[128];
        ESCAPE['"'] = '"';
        ESCAPE['\\'] = '\\';
        ESCAPE[0x08] = 'b';
        ESCAPE[0x09] = 't';
        ESCAPE[0x0C] = 'f';
        ESCAPE[0x0A] = 'n';
        ESCAPE[0x0D] = 'r';
    }

    private static final int STATE_ARRAY = 0;
    private static final int STATE_OBJ_KEY = 1;
    private static final int STATE_OBJ_VALUE = 2;
    private static final int STATE_INIT = 3;

    private final OutputStream out;
    private final FloatingPointFormatter numberPropertyFormatter;

    private final byte[] buf;
    private int pos;

    private int state;
    private long stack;
    private boolean comma;

    public HakunaJsonWriter(OutputStream out, FloatingPointFormatter formatter) {
        this.out = out;
        this.numberPropertyFormatter = formatter;
        this.buf = new byte[BUF_LEN];
        this.pos = 0;
        this.state = STATE_INIT;
        this.stack = state;
        this.comma = false;
    }

    public boolean insideArray() {
        return state == 0;
    }

    public void writeStartObject() throws IOException {
        if (pos + 2 >= BUF_LEN) {
            flush();
        }
        if (comma && state == STATE_ARRAY) {
            buf[pos++] = COMMA;
        }
        buf[pos++] = START_OBJ;
        state = STATE_OBJ_KEY;
        stack = (stack << 2) | state;
        comma = false;
    }

    public void writeEndObject() throws IOException {
        if (state != STATE_OBJ_KEY) {
            throw new IllegalStateException();
        }
        if (pos + 1 >= BUF_LEN) {
            flush();
        }
        buf[pos++] = END_OBJ;
        comma = true;
        stack >>>= 2;
        state = (int) (stack & 3L);
    }

    public void writeStartArray() throws IOException {
        if (pos + 2 >= BUF_LEN) {
            flush();
        }
        if (comma && state == STATE_ARRAY) {
            buf[pos++] = COMMA;
        }
        buf[pos++] = START_ARR;
        state = STATE_ARRAY;
        stack = stack << 2;
        comma = false;
    }

    public void writeEndArray() throws IOException {
        if (state != STATE_ARRAY) {
            throw new IllegalStateException();
        }
        if (pos + 1 >= BUF_LEN) {
            flush();
        }
        buf[pos++] = END_ARR;
        comma = true;
        stack >>>= 2;
        state = (int) (stack & 3L);
    }

    public void writeStringField(byte[] key, String value) throws IOException {
        writeFieldName(key);
        writeString(value);
    }

    public void writeFieldName(byte[] utf8) throws IOException {
        if (state != STATE_OBJ_KEY) {
            throw new IllegalStateException();
        }
        int len = utf8.length;
        if (pos + len + 4 >= BUF_LEN) {
            flush();
        }
        if (comma) {
            buf[pos++] = COMMA;
        }
        buf[pos++] = QUOTE;
        System.arraycopy(utf8, 0, buf, pos, len);
        pos += len;
        buf[pos++] = QUOTE;
        buf[pos++] = COLON;
        comma = false;
        state = STATE_OBJ_VALUE;
    }

    public void writeString(String s) throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            int len = UTF8.getLength(s);
            int countEscape = getCountEscape(s);
            if (pos + len + countEscape + 2 >= BUF_LEN) {
                flush();
            }
            buf[pos++] = QUOTE;
            if (len == s.length()) {
                if (countEscape == 0) {
                    writeASCII(s, len);
                } else {
                    writeASCIIEscape(s, len);
                }
            } else {
                if (countEscape == 0) {
                    writeUTF8(s);
                } else {
                    writeUTF8Escape(s);
                }
            }
            buf[pos++] = QUOTE;
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
        break;
        default:
            throw new IllegalStateException();
        }
    }

    private static int getCountEscape(String s) {
        int count = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c < 0x80 && ESCAPE[c] != 0) {
                count++;
            }
        }
        return count;
    }

    public void writeStringUnsafe(String s) throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            int len = UTF8.getLength(s);
            if (pos + len + 2 >= BUF_LEN) {
                flush();
            }
            buf[pos++] = QUOTE;
            if (len == s.length()) {
                writeASCII(s, len);
            } else {
                writeUTF8(s);
            }
            buf[pos++] = QUOTE;
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
        break;
        default:
            throw new IllegalStateException();
        }
    }

    public void writeStringUnsafe(byte[] utf8) throws IOException {
        writeStringUnsafe(utf8, 0, utf8.length);
    }

    public void writeStringUnsafe(byte[] utf8, int off, int len) throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            if (pos + len + 2 >= BUF_LEN) {
                flush();
            }
            buf[pos++] = QUOTE;
            System.arraycopy(utf8, off, buf, pos, len);
            pos += len;
            buf[pos++] = QUOTE;
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
        break;
        default:
            throw new IllegalStateException();
        }
    }

    public void writeNullField(byte[] name) throws IOException {
        writeFieldName(name);
        writeNull();
    }

    public void writeNull() throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            if (pos + 4 >= BUF_LEN) {
                flush();
            }
            System.arraycopy(NULL, 0, buf, pos, 4);
            pos += 4;
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
            break;
        default:
            throw new IllegalStateException();
        }
    }

    public void writeBoolean(boolean b) throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            if (pos + 5 >= BUF_LEN) {
                flush();
            }
            byte[] a = b ? TRUE : FALSE;
            int len = a.length;
            System.arraycopy(a, 0, buf, pos, len);
            pos += len;
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
            break;
        default:
            throw new IllegalStateException();
        }
    }

    public void writeNumber(int v) throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            if (pos + 11 >= BUF_LEN) {
                flush();
            }
            pos = NumberOutput.outputInt(v, buf, pos);
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
            break;
        default:
            throw new IllegalStateException();
        }
    }

    public void writeNumber(long v) throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            if (pos + 21 >= BUF_LEN) {
                flush();
            }
            pos = NumberOutput.outputLong(v, buf, pos);
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
            break;
        default:
            throw new IllegalStateException();
        }
    }

    public void writeNumber(float v) throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            // max char length for long (21) + dot (1) + maxDecimals
            if (pos + 22 + numberPropertyFormatter.maxDecimalsFloat() >= BUF_LEN) {
                flush();
            }
            pos = numberPropertyFormatter.writeFloat(v, buf, pos);
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
            break;
        default:
            throw new IllegalStateException();
        }
    }

    public void writeNumber(double v) throws IOException {
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            // max char length for long (21) + dot (1) + maxDecimals
            if (pos + 22 + numberPropertyFormatter.maxDecimalsDouble() >= BUF_LEN) {
                flush();
            }
            pos = numberPropertyFormatter.writeDouble(v, buf, pos);
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
            break;
        default:
            throw new IllegalStateException();
        }
    }

    public void writeCoordinate(double x, double y) throws IOException {
        writeCoordinate(x, y, numberPropertyFormatter);
    }
    public void writeCoordinate(double x, double y, FloatingPointFormatter f) throws IOException {
        //       ,[    x   ,    y   ]
        if (pos + 2 + 22 + 1 + 22 + 1 +  2 * f.maxDecimalsOrdinate() >= BUF_LEN) {
            flush();
        }
        if (comma && state == STATE_ARRAY) {
            buf[pos++] = COMMA;
        }
        buf[pos++] = START_ARR;
        pos = f.writeOrdinate(x, buf, pos);
        buf[pos++] = COMMA;
        pos = f.writeOrdinate(y, buf, pos);
        buf[pos++] = END_ARR;
        comma = true;
        state = (int) (stack & 1L);
    }

    public void writeCoordinate(double x, double y, double z) throws IOException {
        writeCoordinate( x, y, z, numberPropertyFormatter);
    }
    public void writeCoordinate(double x, double y, double z, FloatingPointFormatter f) throws IOException {
        //       ,[    x   ,    y   ,    z]
        if (pos + 2 + 22 + 1 + 22 + 1 + 22 + 3 * f.maxDecimalsOrdinate() >= BUF_LEN) {
            flush();
        }
        if (comma && state == STATE_ARRAY) {
            buf[pos++] = COMMA;
        }
        buf[pos++] = START_ARR;
        pos = f.writeOrdinate(x, buf, pos);
        buf[pos++] = COMMA;
        pos = f.writeOrdinate(y, buf, pos);
        buf[pos++] = COMMA;
        pos = f.writeOrdinate(z, buf, pos);
        buf[pos++] = END_ARR;
        comma = true;
        state = (int) (stack & 1L);
    }

    public void writeCoordinate(double x, double y, double z, double m) throws IOException {
        writeCoordinate(x, y, z, m, numberPropertyFormatter);
    }
    public void writeCoordinate(double x, double y, double z, double m, FloatingPointFormatter f) throws IOException {
        //       ,[    x   ,    y   ,    z   ,    m]
        if (pos + 2 + 22 + 1 + 22 + 1 + 22 + 1 + 22 + 4 * f.maxDecimalsOrdinate() >= BUF_LEN) {
            flush();
        }
        if (comma && state == STATE_ARRAY) {
            buf[pos++] = COMMA;
        }
        buf[pos++] = START_ARR;
        pos = f.writeOrdinate(x, buf, pos);
        buf[pos++] = COMMA;
        pos = f.writeOrdinate(y, buf, pos);
        buf[pos++] = COMMA;
        pos = f.writeOrdinate(z, buf, pos);
        buf[pos++] = COMMA;
        pos = numberPropertyFormatter.writeOrdinate(m, buf, pos);
        buf[pos++] = END_ARR;
        comma = true;
        state = (int) (stack & 1L);
    }

    protected void writeUTF8(String s) {
        int len = s.length();
        int i = 0;

        for (; i < len; i++) {
            char ch = s.charAt(i);
            if (ch >= 0x80) {
                break;
            }
            buf[pos++] = (byte) ch;
        }

        for (; i < len; i++) {
            char ch = s.charAt(i);
            if (ch < 0x80) {
                buf[pos++] = (byte) ch;
            } else if (ch < 0x800) {
                buf[pos++] = (byte) (0xc0 | (ch >> 6));
                buf[pos++] = (byte) (0x80 | (ch & 0x3f));
            } else {
                if (Character.isSurrogate(ch)) {
                    char low = s.charAt(++i);
                    int c = Character.toCodePoint(ch, low);
                    buf[pos++] = (byte) (0xf0 | (c >> 18));
                    buf[pos++] = (byte) (0x80 | ((c >> 12) & 0x3f));
                    buf[pos++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    buf[pos++] = (byte) (0x80 | (c & 0x3f));
                } else {
                    buf[pos++] = (byte) (0xe0 | (ch >> 12));
                    buf[pos++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
                    buf[pos++] = (byte) (0x80 | (ch & 0x3f));
                }
            }
        }
    }

    protected void writeUTF8Escape(String s) {
        int len = s.length();
        int i = 0;

        for (; i < len; i++) {
            int ch = s.charAt(i);
            if (ch >= 0x80 || ESCAPE[ch] != 0) {
                break;
            }
            buf[pos++] = (byte) ch;
        }

        for (; i < len; i++) {
            char ch = s.charAt(i);
            if (ch < 0x80) {
                byte escape = ESCAPE[ch];
                if (escape == 0) {
                    buf[pos++] = (byte) ch;
                } else {
                    buf[pos++] = '\\';
                    buf[pos++] = escape;
                }
            } else if (ch < 0x800) {
                buf[pos++] = (byte) (0xc0 | (ch >> 6));
                buf[pos++] = (byte) (0x80 | (ch & 0x3f));
            } else {
                if (Character.isSurrogate(ch)) {
                    int c = Character.toCodePoint(ch, s.charAt(++i));
                    buf[pos++] = (byte) (0xf0 | (c >> 18));
                    buf[pos++] = (byte) (0x80 | ((c >> 12) & 0x3f));
                    buf[pos++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    buf[pos++] = (byte) (0x80 | (c & 0x3f));
                } else {
                    buf[pos++] = (byte) (0xe0 | (ch >> 12));
                    buf[pos++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
                    buf[pos++] = (byte) (0x80 | (ch & 0x3f));
                }
            }
        }
    }

    protected void writeASCII(String s, int len) {
        for (int i = 0; i < len; i++) {
            buf[pos++] = (byte) s.charAt(i);
        }
    }

    protected void writeASCIIEscape(String s, int len) {
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            byte escape = ESCAPE[c];
            if (escape == 0) {
                buf[pos++] = (byte) c;
            } else {
                buf[pos++] = '\\';
                buf[pos++] = escape;
            }
        }
    }

    public void writeRecordSeparator(byte b) throws IOException {
        if (pos + 1 >= BUF_LEN) {
            flush();
        }
        buf[pos++] = b;
    }
    
    public void writeJson(byte[] bytes) throws IOException {
        // write as is
        switch (state) {
        case STATE_ARRAY:
            if (pos + 1 >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = COMMA;
            }
        case STATE_OBJ_VALUE:
            flush();
            byte[] a = bytes;
            int len = a.length;
            out.write(a,0,len);
            comma = true;
            state >>>= 1; // STATE_ARRAY => STATE_ARRAY, STATE_OBJ_VALUE => STATE_OBJ_KEY
            break;
        default:
            throw new IllegalStateException();
        }
    }

    public void flush() throws IOException {
        int n = pos;
        if (n > 0) {
            pos = 0;
            out.write(buf, 0, n);
        }
    }

    public void close() throws IOException {
        flush();
    }

}
