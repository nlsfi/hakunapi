package fi.nls.hakunapi.core.util;

import java.time.LocalDate;

import com.fasterxml.jackson.core.io.NumberOutput;

public class LocalDateOutput {

    // year (10) + month (2) + day (2) + separators (2)
    // 10 characters for year allows for a range of [-999999999, Integer.MAX_VALUE]
    public static final int MAX_BYTE_LEN = 16;

    public static int outputLocalDate(LocalDate value, byte[] buf, int pos) {
        int y = value.getYear();
        int m = value.getMonthValue();
        int d = value.getDayOfMonth();

        pos = NumberOutput.outputInt(y, buf, pos);

        buf[pos++] = '-';
        if (m < 10) {
            buf[pos++] = '0';
            buf[pos++] = (byte) ('0' + m);
        } else {
            buf[pos++] = (byte) '1';
            buf[pos++] = (byte) ('0' - 10 + m);
        }

        buf[pos++] = '-';
        if (d < 10) {
            buf[pos++] = '0';
            buf[pos++] = (byte) ('0' + d);
        } else if (d < 20) {
            buf[pos++] = (byte) '1';
            buf[pos++] = (byte) ('0' - 10 + d);
        } else if (d < 30) {
            buf[pos++] = (byte) '2';
            buf[pos++] = (byte) ('0' - 20 + d);
        } else {
            buf[pos++] = (byte) '3';
            buf[pos++] = (byte) ('0' - 30 + d);
        }

        return pos;
    }

}
