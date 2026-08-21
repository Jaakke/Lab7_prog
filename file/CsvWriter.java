package file;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import models.*;

public class CsvWriter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_ZONED_DATE_TIME;

    private static final String CSV_DELIMITER = ",";
    private static final String INNER_DELIMITER = ";";

    public static void writeToFile(String filename, LinkedHashMap<Long, StudyGroup> collection)
            throws IOException {

        File file = prepareAndValidateFile(filename);

        int savedCount = writeCollectionToFile(file, collection);

        System.out.println("Сохранено " + savedCount + " из " + collection.size() +
                " элементов в файл " + filename);
    }

    private static File prepareAndValidateFile(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        File file = new File(filename);

        if (file.exists() && !file.canWrite()) {
            throw new IOException("Нет прав на запись в файл " + filename);
        }

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Не удалось создать директории для файла " + filename);
        }

        return file;
    }

    private static int writeCollectionToFile(File file, LinkedHashMap<Long, StudyGroup> collection) throws IOException {
        int savedCount = 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(getHeader());
            writer.newLine();

            for (Map.Entry<Long, StudyGroup> entry : collection.entrySet()) {
                if (writeEntry(writer, entry.getKey(), entry.getValue())) {
                    savedCount++;
                }
            }
            writer.flush();
        }
        return savedCount;
    }

    private static boolean writeEntry(BufferedWriter writer, Long key, StudyGroup group) {
        try {
            String csvLine = convertToCsvLine(group);
            writer.write(csvLine);
            writer.newLine();
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении элемента с ID " + key + ": " + e.getMessage());
            return false;
        }
    }

    public static void createBackup(String filename) throws IOException {
        File original = new File(filename);
        if (!original.exists()) return;

        String backupName = filename + ".backup." + System.currentTimeMillis();
        File backup = new File(backupName);

        copyFileContent(original, backup);
        System.out.println("Создана резервная копия: " + backupName);
    }

    private static void copyFileContent(File source, File destination) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(source));
             BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        }
    }

    public static boolean canWrite(String filename) {
        if (filename == null || filename.trim().isEmpty()) return false;

        File file = new File(filename);

        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                return parent == null || parent.exists() || parent.mkdirs();
            } catch (SecurityException e) {
                return false;
            }
        }

        return file.canWrite() && !file.isDirectory();
    }

    private static String getHeader() {
        return String.join(CSV_DELIMITER,
                "id", "name",
                "coordinates.x", "coordinates.y",
                "coordinates.y",
                "creationDate", "studentsCount", "expelledStudents",
                "formOfEducation", "semesterEnum",
                "person.name", "person.birthday", "person.eyeColor", "person.location"
        );
    }

    private static String convertToCsvLine(StudyGroup group) {
        StringBuilder sb = new StringBuilder();

        sb.append(group.getId()).append(CSV_DELIMITER);
        sb.append(escapeCsv(group.getName())).append(CSV_DELIMITER);

        Coordinates coords = group.getCoordinates();
        sb.append(coords.getX()).append(CSV_DELIMITER);
        sb.append(coords.getY()).append(CSV_DELIMITER);
        sb.append(CSV_DELIMITER);

        sb.append(formatDate(group.getCreationDate())).append(CSV_DELIMITER);
        sb.append(group.getStudentsCount()).append(CSV_DELIMITER);
        sb.append(group.getExpelledStudents()).append(CSV_DELIMITER);
        sb.append(group.getFormOfEducation().name()).append(CSV_DELIMITER);

        Semester semester = group.getSemesterEnum();
        sb.append(semester == null ? "null" : semester.name()).append(CSV_DELIMITER);

        Person person = group.getGroupAdmin();
        sb.append(escapeCsv(person.getName())).append(CSV_DELIMITER);
        sb.append(formatDate(person.getBirthday())).append(CSV_DELIMITER);
        sb.append(person.getEyeColor().name()).append(CSV_DELIMITER);

        Location location = person.getLocation();
        if (location == null) {
            sb.append("null");
        } else {
            sb.append(escapeCsv(formatLocation(location)));
        }

        return sb.toString();
    }

    private static String formatLocation(Location location) {
        return String.join(INNER_DELIMITER,
                String.valueOf(location.getX()),
                String.valueOf(location.getY()),
                String.valueOf(location.getZ()),
                escapeCsv(location.getName() == null ? "" : location.getName())
        );
    }

    private static String formatDate(ZonedDateTime date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private static String formatDate(Date date) {
        if (date == null) return "";
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .format(DATE_FORMATTER);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";

        if (value.contains(CSV_DELIMITER) || value.contains("\"") ||
                value.contains("\n") || value.contains("\r")) {

            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}