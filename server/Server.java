package server;

import commands.CommandInvoker;
import network.Request;
import network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public record Server(int port, CommandInvoker commandInvoker) {

    public void run() {
        startConsoleThread();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                     ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

                    oos.flush();

                    System.out.println("Новое подключение: " + clientSocket.getInetAddress());

                    Request request = (Request) ois.readObject();

                    if (request.getCommandName().equalsIgnoreCase("save")) {
                        oos.writeObject(new Response("Ошибка: команда save доступна только на сервере!"));
                        continue;
                    }

                    Response response = commandInvoker.handle(request);

                    oos.writeObject(response);
                    oos.flush();

                } catch (ClassNotFoundException e) {
                    System.err.println("Ошибка при десериализации данных: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось запустить сервер: " + e.getMessage());
        }
    }

    private void startConsoleThread() {
        Thread consoleThread = new Thread(() -> {
            Scanner serverScanner = new Scanner(System.in);
            System.out.println("Консоль сервера готова к приему команд (save, exit)...");

            while (serverScanner.hasNextLine()) {
                String line = serverScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("save")) {
                    Request saveRequest = new Request("save", "", null, "server", "server_pass");
                    Response response = commandInvoker.handle(saveRequest);
                    System.out.println("[Консоль Сервера] " + response.getMessage());
                } else if (line.equalsIgnoreCase("exit")) {
                    System.out.println("[Консоль Сервера] Завершение работы сервера...");
                    System.exit(0);
                } else {
                    System.out.println("[Консоль Сервера] Неизвестная команда. Доступны только: save, exit");
                }
            }
        });

        consoleThread.setDaemon(true);
        consoleThread.start();
    }
}