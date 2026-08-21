package client;

import network.Request;
import network.Response;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class ClientApp {
    private final String host;
    private final int port;
    private final ConsoleHandler consoleHandler;

    public static String username = "";
    public static String password = "";

    public ClientApp(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        ClientApp.username = username;
        ClientApp.password = password;
        this.consoleHandler = new ConsoleHandler(username, password);
    }

    public void run() {
        System.out.println("Подключение к серверу...");
        System.out.println("Клиент готов к работе. Для получения списка команд введите 'help'.");
        startCommandLoop();
    }

    private void startCommandLoop() {
        while (true) {
            Request request = consoleHandler.handle();
            if (request == null) continue;

            if (request.getCommandName().equalsIgnoreCase("exit")) {
                System.out.println("Завершение работы клиента.");
                break;
            }

            try {
                if (request.getCommandName().equalsIgnoreCase("update")) {
                    Response checkResponse = sendAndReceive(request);

                    if (checkResponse != null && "READY".equals(checkResponse.getMessage())) {
                        models.StudyGroup group = consoleHandler.askStudyGroup();
                        if (group == null) continue; // Отмена ввода при ошибке

                        Request updateRequest = new Request("update", request.getArgument(), group, username, password);
                        Response finalResponse = sendAndReceive(updateRequest);
                        handleServerResponse(finalResponse);
                    } else {
                        handleServerResponse(checkResponse);
                    }
                    continue;
                }

                Response response = sendAndReceive(request);
                handleServerResponse(response);
            } catch (Exception e) {
                System.err.println("Ошибка связи с сервером: " + e.getMessage());
            }
        }
    }

    private Response sendAndReceive(Request request) throws IOException, ClassNotFoundException {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(host, port));

            ObjectOutputStream oos = new ObjectOutputStream(socketChannel.socket().getOutputStream());
            oos.writeObject(request);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(socketChannel.socket().getInputStream());
            return (Response) ois.readObject();
        }
    }

    private void handleServerResponse(Response response) {
        if (response != null && response.getMessage() != null) {
            System.out.println(response.getMessage());
        } else {
            System.out.println("Получен пустой ответ от сервера.");
        }
    }
}