package client;

import java.util.Scanner;

public class ClientMain {
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 19196;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Запуск Клиентского Приложения ===");

        String host = parseHost(args);
        int port = parsePort(args, scanner);

        System.out.println("\n=== Авторизация пользователя ===");
        System.out.print("Введите логин: ");
        String username = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.err.println("Ошибка: Логин и пароль не могут быть пустыми!");
            return;
        }

        ClientApp clientApp = new ClientApp(host, port, username, password);
        clientApp.run();
    }

    private static String parseHost(String[] args) {
        if (args.length >= 1 && args[0] != null && !args[0].trim().isEmpty()) {
            return args[0].trim();
        }
        return DEFAULT_HOST;
    }

    private static int parsePort(String[] args, Scanner scanner) {
        if (args.length >= 2) {
            try {
                int p = Integer.parseInt(args[1].trim());
                if (p >= MIN_PORT && p <= MAX_PORT) return p;
            } catch (NumberFormatException ignored) {}
        }

        while (true) {
            try {
                System.out.print("Введите порт сервера: ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    return DEFAULT_PORT;
                }
                int p = Integer.parseInt(input);
                if (p >= MIN_PORT && p <= MAX_PORT) {
                    return p;
                }
                System.out.println("Порт должен быть в диапазоне от " + MIN_PORT + " до " + MAX_PORT);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число.");
            }
        }
    }
}