package top.mothership.cb.mirror.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static long getSecondFromOsuDateString(String dateTime, Integer timeZone) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.parse(dateTime, formatter);
        return time.toInstant(ZoneOffset.ofHours(timeZone)).toEpochMilli() / 1000L;
    }

    public static String toOsuDateString(long timestampSecond, Integer timeZone) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Instant time = Instant.ofEpochSecond(timestampSecond);
        LocalDateTime time1 = LocalDateTime.ofInstant(time, ZoneOffset.ofHours(timeZone));
        return formatter.format(time1);
    }

    public static boolean isOneMonthEarlier(long timestamp) {
        return System.currentTimeMillis() / 1000 - timestamp > 2592000L;
    }

    public static boolean isOneWeekEarlier(long timestamp) {
        return System.currentTimeMillis() / 1000 - timestamp > 604800L;
    }
}
