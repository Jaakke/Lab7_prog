package file;

import models.*;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CsvReader {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_ZONED_DATE_TIME;

    private static final String CSV_DELIMITER = ",";

    private static final String INNER_DELIMITER = ";";

    public static LinkedHashMap<Long, StudyGroup> readFromFile(String filename)
            throws IOException, IllegalArgumentException {

        File file = new File(filename);
        validateFile(file);

        LinkedHashMap<Long, StudyGroup> collection = new LinkedHashMap<>();
        int[] stats = parseCsvFile(file, collection);
        int successCount = stats[0];
        int errorCount = stats[1];

        System.out.println("Загрузка завершена. Успешно: " + successCount +
                ", ошибок: " + errorCount);

        validateParsingResult(collection, errorCount);

        return collection;
    }

    private static void validateFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Файл " + file.getName() + " не найден");
        }

        if (!file.canRead()) {
            throw new IOException("Нет прав на чтение файла " + file.getName());
        }

        if (file.isDirectory()) {
            throw new IOException("Указанный путь является директорией, а не файлом");
        }
    }

    private static int[] parseCsvFile(File file, LinkedHashMap<Long, StudyGroup> collection) throws IOException {
        int lineNumber = 0;
        int successCount = 0;
        int errorCount = 0;
        boolean isFirstLine = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;
                    if (isHeaderLine(line)) {
                        continue;
                    }
                }

                if (processLine(line, lineNumber, collection)) {
                    successCount++;
                } else {
                    errorCount++;
                }
            }
        }
        return new int[]{successCount, errorCount};
    }

    private static boolean isHeaderLine(String line) {
        return line.startsWith("id") || line.startsWith("ID");
    }

    private static boolean processLine(String line, int lineNumber, LinkedHashMap<Long, StudyGroup> collection) {
        try {
            StudyGroup group = parseStudyGroup(line, lineNumber);
            Long id = group.getId();

            if (collection.containsKey(id)) {
                System.err.println("Строка " + lineNumber +
                        ": предупреждение - дубликат ID " + id +
                        ". Элемент будет пропущен");
                return false;
            }

            collection.put(id, group);
            return true;

        } catch (Exception e) {
            System.err.println("Ошибка в строке " + lineNumber + ": " + e.getMessage());
            return false;
        }
    }

    private static void validateParsingResult(LinkedHashMap<Long, StudyGroup> collection, int errorCount) {
        if (collection.isEmpty() && errorCount > 0) {
            throw new IllegalArgumentException("Не удалось загрузить ни одного элемента");
        }
    }

    private static StudyGroup parseStudyGroup(String csvLine, int lineNumber)
            throws IllegalArgumentException {

        String[] fields = csvLine.split(CSV_DELIMITER, -1);

        if (fields.length < 5) {
            throw new IllegalArgumentException("Недостаточно полей. Ожидается минимум 5, получено " +
                    fields.length);
        }

        try {
            Long id = parseLong(fields[0].trim(), "ID", lineNumber);
            String name = parseString(fields[1].trim(), "name", lineNumber, false);

            Coordinates coordinates = parseCoordinates(fields, lineNumber);

            ZonedDateTime creationDate = parseDate(fields[5].trim(), "creationDate", lineNumber);
            long studentsCount = parseLong(fields[6].trim(), "studentsCount", lineNumber);
            Integer expelledStudents = parseInteger(fields[7].trim(), "expelledStudents", lineNumber, true);
            FormOfEducation formOfEducation = parseFormOfEducation(fields[8].trim(), lineNumber);
            Semester semesterEnum = parseSemester(fields[9].trim(), lineNumber);
            Location location = parseLocation(fields, lineNumber);
            Person groupAdmin = parsePerson(fields, lineNumber, location);

            return new StudyGroup(
                    id, name, coordinates, creationDate, studentsCount,
                    expelledStudents, formOfEducation, semesterEnum, groupAdmin
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка преобразования числа: " + e.getMessage());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ошибка парсинга даты: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Неизвестная ошибка: " + e.getMessage());
        }
    }

    private static Coordinates parseCoordinates(String[] csvParts, int lineNumber) {
        try {
            int x = Integer.parseInt(csvParts[2]);
            Float y = Float.parseFloat(csvParts[3]);

            return new Coordinates(x, y);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка в координатах на строке " + lineNumber);
        }
    }

    private static Location parseLocation(String[] csvParts, int lineNumber) {
        try {
            if (csvParts[12].equals("null")) return null;

            Long x = Long.parseLong(csvParts[12]);
            Long y = Long.parseLong(csvParts[13]);
            Float z = Float.parseFloat(csvParts[14]);
            String locName = csvParts[15].equals("null") ? null : csvParts[15];

            return new Location(x, y, z, locName);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private static Person parsePerson(String[] parts, int lineNumber, Location location) {
        try {
            String name = parts[10].trim();

            java.util.Date birthday = new java.util.Date();

            String colorValue = parts[12].trim().toUpperCase();
            Color eyeColor = Color.valueOf(colorValue);

            return new Person(name, birthday, eyeColor, location);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка в данных админа на строке " + lineNumber + ": '" + parts[11] + "' не является цветом. " + e.getMessage());
        }
    }

    private static Long parseLong(String value, String fieldName, int lineNumber) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        try {
            Long result = Long.parseLong(value);
            if (result <= 0) {
                throw new IllegalArgumentException(fieldName + " должно быть > 0");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " должен быть целым числом");
        }
    }

    private static Integer parseInteger(String value, String fieldName, int lineNumber,
                                       boolean canBeNull) {
        if (value == null || value.isEmpty()) {
            if (canBeNull) {
                return null;
            }
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        try {
            Integer result = Integer.parseInt(value);
            if (result <= 0) {
                throw new IllegalArgumentException(fieldName + " должно быть > 0");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " должен быть целым числом");
        }
    }

    private static Float parseFloat(String value, String fieldName, int lineNumber,
                                   boolean canBeNull) {
        if (value == null || value.isEmpty()) {
            if (canBeNull) {
                return null;
            }
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " должен быть числом с плавающей точкой");
        }
    }

    private static String parseString(String value, String fieldName, int lineNumber,
                                     boolean canBeEmpty) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
        if (!canBeEmpty && value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        return value;
    }

    private static ZonedDateTime parseDate(String value, String fieldName, int lineNumber) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        try {
            return ZonedDateTime.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " имеет неверный формат даты");
        }
    }

    private static Date parseDateAsDate(String value, String fieldName, int lineNumber) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(value, DATE_FORMATTER);
            return Date.from(zdt.toInstant());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " имеет неверный формат даты");
        }
    }

    private static FormOfEducation parseFormOfEducation(String value, int lineNumber) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("formOfEducation не может быть пустым");
        }
        try {
            return FormOfEducation.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("formOfEducation должно быть одним из: " +
                                             Arrays.toString(FormOfEducation.values()));
        }
    }

    private static Semester parseSemester(String value, int lineNumber) {
        if (value == null || value.isEmpty() || value.equals("null")) {
            return null;
        }
        try {
            return Semester.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("semester должно быть одним из: " +
                                             Arrays.toString(Semester.values()) + " или null");
        }
    }

    private static Color parseColor(String value, int lineNumber) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("eyeColor не может быть пустым");
        }
        try {
            return Color.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("eyeColor должно быть одним из: " +
                                             Arrays.toString(Color.values()));
        }
    }
}