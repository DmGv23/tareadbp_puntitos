package utec.apitester.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static Date newDateFromToday(Integer addDays, Integer setTime) {
        Calendar cal = Calendar.getInstance();

        if (addDays != null) {
            cal.add(Calendar.DAY_OF_MONTH, addDays);
        }

        if (setTime != null) {
            int hours = setTime / 100;
            int minutes = setTime % 100;

            cal.set(Calendar.HOUR_OF_DAY, hours);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }

        return cal.getTime();
    }

    public static String toISO(Date date) {
        Instant instant = date.toInstant();
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}