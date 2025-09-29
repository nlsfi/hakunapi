package fi.nls.hakunapi.csv;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.io.NumberOutput;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.util.LocalDateOutput;
import fi.nls.hakunapi.core.util.UTF8;

public class CSVWriter implements AutoCloseable, Flushable {

    private static final int BUF_LEN = 8192;

    private static final byte[] FALSE = "FALSE".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] TRUE = "TRUE".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] NULL = "NULL".getBytes(StandardCharsets.US_ASCII);

    private static final byte QUOTE = '"';
    private static final byte COMMA = ',';
    private static final byte LF = '\n';

    private static final byte[] POINT = "POINT".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] LINESTRING = "LINESTRING".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] POLYGON = "POLYGON".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] MULTIPOINT = "MULTIPOINT".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] MULTILINESTRING = "MULTILINESTRING".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] MULTIPOLYGON = "MULTIPOLYGON".getBytes(StandardCharsets.US_ASCII);

    private static final byte[] Z = " Z".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ZM = " ZM".getBytes(StandardCharsets.US_ASCII);

    private static final byte L_PAREN = '(';
    private static final byte R_PAREN = ')';

    private final OutputStream out;
    private final FloatingPointFormatter formatter;
    private final StringBuilder strBuf;

    private final byte[] buf;
    private int pos;

    private int columnCount;
    private int column;

    public CSVWriter(OutputStream out, FloatingPointFormatter formatter) {
        this.out = out;
        this.formatter = formatter;
        this.strBuf = new StringBuilder(32);
        this.buf = new byte[BUF_LEN];
    }

    public void init(String[] columns) throws IOException {
        this.columnCount = columns.length;
        for (int i = 0; i < columnCount; i++) {
            String column = columns[i];
            byte[] utf8 = column.getBytes(StandardCharsets.UTF_8);
            int len = utf8.length;
            if (pos + 1 + len >= BUF_LEN) {
                flush();
            }
            System.arraycopy(utf8, 0, buf, pos, len);
            pos += len;
            buf[pos++] = i + 1 == columnCount ? LF : COMMA;
        }
    }

    @Override
    public void flush() throws IOException {
        int n = pos;
        if (n > 0) {
            pos = 0;
            out.write(buf, 0, n);
        }
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    void writeCommaOrLineFeed() {
        if (++column == columnCount) {
            buf[pos++] = LF;
            column = 0;
        } else {
            buf[pos++] = COMMA;
        }
    }

    public void writeNull() throws IOException {
        if (pos + 5 >= BUF_LEN) {
            flush();
        }
        System.arraycopy(NULL, 0, buf, pos, 4);
        writeCommaOrLineFeed();
    }

    public void writeBoolean(boolean b) throws IOException {
        if (pos + 6 >= BUF_LEN) {
            flush();
        }
        byte[] a = b ? TRUE : FALSE;
        int len = a.length;
        System.arraycopy(a, 0, buf, pos, len);
        pos += len;
        writeCommaOrLineFeed();
    }

    public void writeNumber(int v) throws IOException {
        if (pos + 12 >= BUF_LEN) {
            flush();
        }
        pos = NumberOutput.outputInt(v, buf, pos);
        writeCommaOrLineFeed();
    }

    public void writeNumber(long v) throws IOException {
        if (pos + 22 >= BUF_LEN) {
            flush();
        }
        pos = NumberOutput.outputLong(v, buf, pos);
        writeCommaOrLineFeed();
    }

    public void writeNumber(float v) throws IOException {
        if (pos + 23 + formatter.maxDecimalsFloat() >= BUF_LEN) {
            flush();
        }
        pos = formatter.writeFloat(v, buf, pos);
        writeCommaOrLineFeed();
    }

    public void writeNumber(double v) throws IOException {
        if (pos + 23 + formatter.maxDecimalsDouble() >= BUF_LEN) {
            flush();
        }
        pos = formatter.writeDouble(v, buf, pos);
        writeCommaOrLineFeed();
    }

    public void writeLocalDate(LocalDate value) throws IOException {
        if (pos + 3 + LocalDateOutput.MAX_BYTE_LEN >= BUF_LEN) {
            flush();
        }
        buf[pos++] = QUOTE;
        pos = LocalDateOutput.outputLocalDate(value, buf, pos);
        buf[pos++] = QUOTE;
        writeCommaOrLineFeed();
    }

    public void writeInstant(Instant value) throws IOException {
        strBuf.setLength(0);
        DateTimeFormatter.ISO_INSTANT.formatTo(value, strBuf);
        int len = strBuf.length();

        if (pos + 3 + len >= BUF_LEN) {
            flush();
        }
        buf[pos++] = QUOTE;
        writeASCII(strBuf, len);
        buf[pos++] = QUOTE;
        writeCommaOrLineFeed();
    }

    public void writeGeometry(HakunaGeometry geometry) throws Exception {
        geometry.write(new WKT(false));
    }

    public void writeString(String s) throws IOException {
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
        writeCommaOrLineFeed();
    }

    private static int getCountEscape(String s) {
        int count = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '"') {
                count++;
            }
        }
        return count;
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
            if (ch >= 0x80 || ch == '"') {
                break;
            }
            buf[pos++] = (byte) ch;
        }

        for (; i < len; i++) {
            char ch = s.charAt(i);
            if (ch < 0x80) {
                if (ch == '"') {
                    buf[pos++] = '"';
                    buf[pos++] = '"';
                } else {
                    buf[pos++] = (byte) ch;
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

    private void writeASCII(CharSequence s, int len) {
        for (int i = 0; i < len; i++) {
            buf[pos++] = (byte) s.charAt(i);
        }
    }

    private void writeASCIIEscape(String s, int len) {
        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);
            if (c == '"') {
                buf[pos++] = '"';
                buf[pos++] = '"';
            } else {
                buf[pos++] = (byte) c;
            }
        }
    }

    private void writeASCII(byte[] ascii) throws IOException {
        int len = ascii.length;
        if (pos + len >= BUF_LEN) {
            flush();
        }
        System.arraycopy(ascii, 0, buf, pos, len);
        pos += len;
    }

    private void writeASCII(byte ascii) throws IOException {
        if (pos + 1 >= BUF_LEN) {
            flush();
        }
        buf[pos++] = ascii;
    }

    private class WKT implements GeometryWriter {

        private boolean isPoint;
        private boolean extended;
        private boolean comma;

        public WKT(boolean extended) {
            this.extended = extended;
        }

        @Override
        public void init(HakunaGeometryType type, int srid, int dimension) throws Exception {
            writeASCII(QUOTE);

            if (extended) {
                String extension = "SRID=" + srid + ";";
                writeASCII(extension.getBytes(StandardCharsets.US_ASCII));
            }

            switch (type) {
            case POINT:
                isPoint = true;
                writeASCII(POINT);
                break;
            case LINESTRING:
                writeASCII(LINESTRING);
                break;
            case POLYGON:
                writeASCII(POLYGON);
                break;
            case MULTIPOINT:
                writeASCII(MULTIPOINT);
                break;
            case MULTILINESTRING:
                writeASCII(MULTILINESTRING);
                break;
            case MULTIPOLYGON:
                writeASCII(MULTIPOLYGON);
                break;
            case GEOMETRY:
            case GEOMETRYCOLLECTION:
                throw new IllegalArgumentException("Not supported");
            }

            switch (dimension) {
            case 3:
                writeASCII(Z);
                break;
            case 4:
                writeASCII(ZM);
                break;
            }

            if (isPoint) {
                startRing();
            }
        }

        @Override
        public void end() throws Exception {
            if (isPoint) {
                endRing();
            }
            writeASCII(QUOTE);
            writeCommaOrLineFeed();
        }

        @Override
        public void writeCoordinate(double x, double y) throws Exception {
            if (pos + 2 + 2 * (22 + formatter.maxDecimalsOrdinate()) >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = ',';
            }
            pos = formatter.writeOrdinate(x, buf, pos);
            buf[pos++] = ' ';
            pos = formatter.writeOrdinate(y, buf, pos);
            comma = true;
        }

        @Override
        public void writeCoordinate(double x, double y, double z) throws Exception {
            // [,]x y z
            if (pos + 2 + 3 * (22 + formatter.maxDecimalsOrdinate()) >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = ',';
            }
            pos = formatter.writeOrdinate(x, buf, pos);
            buf[pos++] = ' ';
            pos = formatter.writeOrdinate(y, buf, pos);
            buf[pos++] = ' ';
            pos = formatter.writeOrdinate(z, buf, pos);
        }

        @Override
        public void writeCoordinate(double x, double y, double z, double m) throws Exception {
            // [,]x y
            if (pos + 2 + 4 * (22 + formatter.maxDecimalsOrdinate()) >= BUF_LEN) {
                flush();
            }
            if (comma) {
                buf[pos++] = ',';
            }
            pos = formatter.writeOrdinate(x, buf, pos);
            buf[pos++] = ' ';
            pos = formatter.writeOrdinate(y, buf, pos);
            buf[pos++] = ' ';
            pos = formatter.writeOrdinate(z, buf, pos);
            buf[pos++] = ' ';
            pos = formatter.writeOrdinate(m, buf, pos);
        }

        @Override
        public void startRing() throws Exception {
            comma = false;
            writeASCII(L_PAREN);
        }

        @Override
        public void endRing() throws Exception {
            writeASCII(R_PAREN);
        }

    }

}
