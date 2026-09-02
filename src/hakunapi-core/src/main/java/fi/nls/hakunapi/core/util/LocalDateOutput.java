package fi.nls.hakunapi.core.util;

import java.time.LocalDate;

import tools.jackson.core.io.NumberOutput;

public class LocalDateOutput {

    // sign (1) + year (9) + month (2) + day (2) + separators (2)
    // LocalDate's year range is [-999999999, 999999999]
    public static final int MAX_BYTE_LEN = 16;

    private LocalDateOutput() {}

    /**
     * Writes {@code date} as ISO-8601 into {@code buf} starting at {@code pos},
     * producing the exact same bytes as {@link LocalDate#toString()}. The caller
     * must ensure {@link #MAX_BYTE_LEN} bytes are available.
     *
     * @return offset just past the last byte written
     */
    public static int outputLocalDate(LocalDate date, byte[] buf, int pos) {
        return outputLocalDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), buf, pos);
    }

    public static int outputLocalDate(int year, int month, int day, byte[] buf, int pos) {
        pos = outputYear(year, buf, pos);
        buf[pos++] = '-';
        pos = outputTwoDigits(month, buf, pos);
        buf[pos++] = '-';
        return outputTwoDigits(day, buf, pos);
    }

    // LocalDate#toString zero-pads the year to four digits and prefixes a '+' beyond them
    private static int outputYear(int year, byte[] buf, int pos) {
        if (year < 0) {
            buf[pos++] = '-';
            year = -year;
        } else if (year > 9999) {
            buf[pos++] = '+';
        }
        if (year < 1000) {
            buf[pos++] = (byte) ('0' + year / 1000 % 10);
            buf[pos++] = (byte) ('0' + year / 100 % 10);
            buf[pos++] = (byte) ('0' + year / 10 % 10);
            buf[pos++] = (byte) ('0' + year % 10);
            return pos;
        }
        return NumberOutput.outputInt(year, buf, pos);
    }

    private static int outputTwoDigits(int v, byte[] buf, int pos) {
        buf[pos++] = (byte) ('0' + v / 10);
        buf[pos++] = (byte) ('0' + v % 10);
        return pos;
    }

}
