package client;

import models.Color;
import models.Coordinates;
import models.FormOfEducation;
import models.Location;
import models.Person;
import models.Semester;
import models.StudyGroup;
import network.Request;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class ConsoleHandler {
    private final Scanner scanner = new Scanner(System.in);
    private final String username;
    private final String password;

    public ConsoleHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Request handle() {
        System.out.print("> ");
        if (!scanner.hasNextLine()) {
            return new Request("exit", null, null, username, password);
        }

        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return null;

        String[] parts = line.split("\\s+", 2);
        String command = parts[0];
        String arg = parts.length > 1 ? parts[1] : null;

        if (command.equals("insert") || command.equals("remove_lower")) {
            StudyGroup group = askStudyGroup();
            if (group == null) return null;

            return new Request(command, arg, group, username, password);
        }

        return new Request(command, arg, null, username, password);
    }

    public StudyGroup askStudyGroup() {
        try {
            System.out.println("--- Ввод данных StudyGroup ---");

            System.out.print("Введите имя группы: ");
            String name = scanner.nextLine().trim();

            System.out.print("Введите координату X: ");
            int x = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Введите координату Y: ");
            float y = Float.parseFloat(scanner.nextLine().trim());

            Coordinates coordinates = new Coordinates(x, y);

            System.out.print("Введите количество студентов: ");
            long studentsCount = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Введите количество отчисленных студентов: ");
            Integer expelledStudents = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Варианты формы обучения: " + Arrays.toString(FormOfEducation.values()));
            System.out.print("Введите форму обучения: ");
            String formInput = scanner.nextLine().trim().toUpperCase();
            FormOfEducation formOfEducation = FormOfEducation.valueOf(formInput);

            System.out.println("Варианты семестра обучения: " + Arrays.toString(Semester.values()));
            System.out.print("Введите семестр обучения: ");
            String semesterInput = scanner.nextLine().trim().toUpperCase();
            Semester semesterEnum = Semester.valueOf(semesterInput);

            System.out.println("--- Ввод данных админа группы ---");

            System.out.print("Введите имя админа: ");
            String adminName = scanner.nextLine().trim();

            System.out.print("Введите дату рождения админа (дд.мм.гггг): ");
            String birthdayStr = scanner.nextLine().trim();
            Date birthday = new SimpleDateFormat("dd.MM.yyyy").parse(birthdayStr);

            System.out.println("Варианты цвета глаз: " + Arrays.toString(Color.values()));
            System.out.print("Введите цвет глаз: ");
            Color eyeColor = Color.valueOf(scanner.nextLine().trim().toUpperCase());

            Location location = null;
            System.out.print("Хотите ввести локацию? (да/нет): ");
            String locChoice = scanner.nextLine().trim().toLowerCase();

            if (locChoice.equals("да") || locChoice.equals("yes")) {
                System.out.print("Введите координату X локации (long): ");
                Long locX = Long.parseLong(scanner.nextLine().trim());

                System.out.print("Введите координату Y локации (long): ");
                Long locY = Long.parseLong(scanner.nextLine().trim());

                System.out.print("Введите координату Z локации (float): ");
                Float locZ = Float.parseFloat(scanner.nextLine().trim());

                System.out.print("Введите название локации: ");
                String locName = scanner.nextLine().trim();

                location = new Location(locX, locY, locZ, locName);
            }

            Person groupAdmin = new Person(adminName, birthday, eyeColor, location);

            StudyGroup group = new StudyGroup();
            group.setName(name);
            group.setCoordinates(coordinates);
            group.setStudentsCount(studentsCount);
            group.setExpelledStudents(expelledStudents);
            group.setFormOfEducation(formOfEducation);
            group.setSemesterEnum(semesterEnum);
            group.setGroupAdmin(groupAdmin);
            group.setOwner(username);

            return group;
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка ввода: Неверное значение перечисления или числа.");
            return null;
        } catch (Exception e) {
            System.err.println("Ошибка ввода данных объекта: " + e.getMessage());
            return null;
        }
    }
}