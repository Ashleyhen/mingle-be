package com.mingle.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Formatters {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDate toLocalDate(String birthday) {
        return LocalDate.parse(birthday, FORMATTER);
    }

    public static String localDateToString(LocalDate date) {
        return date.format(FORMATTER);
    }
}