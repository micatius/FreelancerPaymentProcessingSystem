package hr.java.production.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class UiUtils {
    private UiUtils() {
    }

    public static Comparator<String> dateStringComparator(DateTimeFormatter formatter) {
        return (dateStr1, dateStr2) -> {
            try {
                LocalDate date1 = LocalDate.parse(dateStr1, formatter);
                LocalDate date2 = LocalDate.parse(dateStr2, formatter);
                return date1.compareTo(date2);
            } catch (Exception _) {
                return dateStr1.compareTo(dateStr2);
            }
        };

    }
}
