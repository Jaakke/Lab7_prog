package models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Semester implements Serializable {
    FIRST,
    SIXTH,
    EIGHTH;

    private static final long serialVersionUID = 1L;

    public static String toNames() {
        return Arrays.stream(Semester.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}