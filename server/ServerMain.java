package server;

import collection.CollectionManager;
import commands.*;
import database.DatabaseManager;
import network.Request;
import network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static int port;
    private static CollectionManager collectionManager;
    private static CommandManager commandManager;
    private static DatabaseManager databaseManager;

    public static void main(String[] args) {
        Scanner startupScanner = new Scanner(System.in);

        System.out.print("Введите порт для запуска сервера (1-65535): ");
        try {
            port = Integer.parseInt(startupScanner.nextLine().trim());
            if (port < 1 || port > 65535) {
                System.out.println("Критическая ошибка: Порт должен быть в диапазоне от 1 до 65535!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Критическая ошибка: Порт должен быть числом!");
            return;
        }

        System.out.print("Введите имя пользователя БД: ");
        String dbUser = startupScanner.nextLine().trim();
        System.out.print("Введите пароль БД: ");
        String dbPassword = startupScanner.nextLine().trim();

        collectionManager = new CollectionManager();

        try {
            String clearPassword = dbPassword.trim();
            databaseManager = new DatabaseManager(dbUser, clearPassword);

            var collectionData = databaseManager.loadCollection();
            if (collectionData == null) {
                System.out.println("[КРИТИЧЕСКАЯ ОШИБКА] Не удалось загрузить коллекцию.");
                return;
            }

            collectionData.forEach((id, group) -> collectionManager.addWithId(id, group));
            System.out.println("Коллекция успешно загружена из БД.");

        } catch (Exception e) {
            System.out.println("[КРИТИЧЕСКАЯ ОШИБКА БД] Ошибка инициализации: " + e.getMessage());
            return;
        }

        commandManager = new CommandManager(collectionManager, databaseManager);

        registerAllCommands();
        startConsoleReaderThread();
        runNetworkLoop();
    }

    private static void registerAllCommands() {
        commandManager.register("help", new HelpCommand(commandManager.getCommands()));
        commandManager.register("info", new InfoCommand(collectionManager));
    }

    private static void startConsoleReaderThread() {
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Консоль сервера активна. Доступная команда: exit");
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().trim().equalsIgnoreCase("exit")) {
                    System.out.println("Завершение работы сервера...");
                    System.exit(0);
                }
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    private static void runNetworkLoop() {
        ExecutorService readPool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер успешно запущен на порту: " + port);

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    readPool.submit(() -> handleClientReading(clientSocket));
                } catch (IOException e) {
                    System.err.println("Ошибка при приеме соединения: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера (не удалось занять порт): " + e.getMessage());
        }
    }

    private static void handleClientReading(Socket clientSocket) {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

            oos.flush();
            Request request = (Request) ois.readObject();
            processAndRespond(oos, request);

        } catch (Exception e) {
            System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private static void processAndRespond(ObjectOutputStream oos, Request request) {
        Response response;

        try {
            if (request == null) {
                response = new Response("Ошибка: Пустой запрос.");
            } else if (databaseManager == null) {
                response = new Response("Внутренняя ошибка сервера: Менеджер базы данных не инициализирован.");
            } else if (!databaseManager.validateUser(request.getUsername(), request.getPassword())) {
                response = new Response("Ошибка: Неверный логин или пароль! Доступ запрещен.");
            } else {
                synchronized (collectionManager) {
                    response = commandManager.execute(request);
                }
            }
        } catch (Exception e) {
            response = new Response("Внутренняя ошибка сервера при обработке команды: " + e.getMessage());
        }

        try {
            oos.writeObject(response);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Не удалось отправить ответ клиенту: " + e.getMessage());
        }
    }
}