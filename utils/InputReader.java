package utils;

import models.*;
import java.util.Date;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class InputReader {
    private final PrimitiveReader primitiveReader;

    public InputReader(Scanner scanner, boolean isInteractiveMode) {
        this.primitiveReader = new PrimitiveReader(scanner, isInteractiveMode);
    }

    public StudyGroup readStudyGroup() {
        primitiveReader.printInteractive("\n=== Ввод данных учебной группы ===");

        String name = primitiveReader.readString("Введите название группы:", false);
        Coordinates coords = readCoordinates();
        long studentsCount = primitiveReader.readLong("Введите количество студентов (>0):", 1, Long.MAX_VALUE);
        Integer expelledStudents = primitiveReader.readInt("Введите количество отчисленных (>0):", 1, Integer.MAX_VALUE);

        FormOfEducation form = primitiveReader.readEnum(
                "Выберите форму обучения (" + FormOfEducation.toNames() + "):", FormOfEducation.class);
        Semester semester = primitiveReader.readEnum(
                "Выберите семестр (" + Semester.toNames() + "):", Semester.class);
        Person groupAdmin = readPerson();

        return new StudyGroup(name, coords, studentsCount, expelledStudents, form, semester, groupAdmin);
    }

    private Coordinates readCoordinates() {
        primitiveReader.printInteractive("--- Ввод координат ---");
        int x = primitiveReader.readInt("Введите координату X (int):", Integer.MIN_VALUE, Integer.MAX_VALUE);
        Float y = primitiveReader.readFloat("Введите координату Y (Float):", -Float.MAX_VALUE, Float.MAX_VALUE);
        return new Coordinates(x, y);
    }

    private Person readPerson() {
        primitiveReader.printInteractive("--- Ввод администратора группы ---");
        String name = primitiveReader.readString("Введите имя админа:", false);
        Date birthday = primitiveReader.readDate("Введите дату рождения (dd.MM.yyyy):");
        Color eyeColor = primitiveReader.readEnum(
                "Выберите цвет глаз (" + Color.toNames() + "):", Color.class);

        boolean hasLocation = primitiveReader.readBoolean("Хотите ли вы указать локацию? (да/нет):");
        Location location = hasLocation ? readLocation() : null;

        return new Person(name, birthday, eyeColor, location);
    }

    private Location readLocation() {
        primitiveReader.printInteractive("--- Ввод локации ---");
        Long x = primitiveReader.readLong("Введите X (Long):", Long.MIN_VALUE, Long.MAX_VALUE);
        Long y = primitiveReader.readLong("Введите Y (Long):", Long.MIN_VALUE, Long.MAX_VALUE);
        Float z = primitiveReader.readFloat("Введите Z (Float):", -Float.MAX_VALUE, Float.MAX_VALUE);
        String name = primitiveReader.readString("Введите название локации (или Enter для null):", true);
        return new Location(x, y, z, name);
    }

    public static class PrimitiveReader {
        private final Scanner scanner;
        private final boolean isInteractiveMode;

        public PrimitiveReader(Scanner scanner, boolean isInteractiveMode) {
            this.scanner = scanner;
            this.isInteractiveMode = isInteractiveMode;
        }

        public void printInteractive(String message) {
            if (isInteractiveMode) {
                System.out.println(message);
            }
        }

        public String readString(String prompt, boolean canBeNull) {
            while (true) {
                printInteractive(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    if (canBeNull) return null;
                    printInteractive("Ошибка: строка не может быть пустой.");
                    continue;
                }
                return input;
            }
        }

        public int readInt(String prompt, int min, int max) {
            while (true) {
                try {
                    printInteractive(prompt + " (диапазон: от " + min + " до " + max + "):");
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) {
                        printInteractive("Ошибка: значение не может быть пустым.");
                        continue;
                    }
                    int value = Integer.parseInt(input);
                    if (value < min || value > max) {
                        printInteractive("Ошибка: число вне диапазона.");
                        continue;
                    }
                    return value;
                } catch (NumberFormatException e) {
                    printInteractive("Ошибка: введите целое число (int).");
                }
            }
        }

        public long readLong(String prompt, long min, long max) {
            while (true) {
                try {
                    printInteractive(prompt + " (минимум: " + min + "):");
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) {
                        printInteractive("Ошибка: значение не может быть пустым.");
                        continue;
                    }
                    long val = Long.parseLong(input);
                    if (val < min || val > max) {
                        printInteractive("Ошибка: число вне допустимого диапазона.");
                        continue;
                    }
                    return val;
                } catch (NumberFormatException e) {
                    printInteractive("Ошибка: введите целое число (long).");
                }
            }
        }

        public Float readFloat(String prompt, float min, float max) {
            while (true) {
                try {
                    printInteractive(prompt);
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) {
                        printInteractive("Ошибка: значение не может быть пустым.");
                        continue;
                    }
                    float val = Float.parseFloat(input);
                    if (val < min || val > max) {
                        printInteractive("Ошибка: число вне допустимого диапазона.");
                        continue;
                    }
                    return val;
                } catch (NumberFormatException e) {
                    printInteractive("Ошибка: введите десятичное число (Float).");
                }
            }
        }

        public <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
            while (true) {
                try {
                    printInteractive(prompt);
                    String input = scanner.nextLine().trim().toUpperCase();
                    if (input.isEmpty()) {
                        printInteractive("Ошибка: выбор не может быть пустым.");
                        continue;
                    }
                    return Enum.valueOf(enumClass, input);
                } catch (IllegalArgumentException e) {
                    printInteractive("Ошибка: значения нет в списке.");
                }
            }
        }

        public Date readDate(String prompt) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            sdf.setLenient(false);
            while (true) {
                try {
                    printInteractive(prompt);
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) {
                        printInteractive("Ошибка: дата не может быть пустой.");
                        continue;
                    }
                    return sdf.parse(input);
                } catch (ParseException e) {
                    printInteractive("Ошибка: формат dd.MM.yyyy (например, 15.05.2001)");
                }
            }
        }

        public boolean readBoolean(String prompt) {
            while (true) {
                printInteractive(prompt);
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("да") || input.equals("yes")) return true;
                if (input.equals("нет") || input.equals("no")) return false;
                printInteractive("Ошибка: ожидается ответ 'да' или 'нет'.");
            }
        }
    }
}