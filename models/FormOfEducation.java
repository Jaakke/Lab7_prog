package models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum FormOfEducation implements Serializable {
    DISTANCE_EDUCATION,
    FULL_TIME_EDUCATION,
    EVENING_CLASSES;

    private static final long serialVersionUID = 1L;

    public static String toNames() {
        return Arrays.stream(FormOfEducation.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}