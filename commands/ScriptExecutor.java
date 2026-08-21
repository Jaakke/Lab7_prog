package commands;

import java.io.*;
import java.util.*;
import network.Request;
import network.Response;
import utils.InputReader;
import models.StudyGroup;

public abstract class ScriptExecutor implements Command {
    private CommandInvoker invoker;
    private Set<String> scriptStack = new HashSet<>();

    public ScriptExecutor(CommandInvoker invoker) {
        this.invoker = invoker;
    }

    public void executeScript(String filename) {
        File file = new File(filename);

        if (!file.exists() || !file.canRead()) {
            System.out.println("Ошибка: файл не существует или недоступен для чтения");
            return;
        }

        if (scriptStack.contains(file.getAbsolutePath())) {
            System.out.println("Ошибка: обнаружена рекурсия при выполнении скрипта");
            return;
        }

        scriptStack.add(file.getAbsolutePath());

        try (Scanner scriptScanner = new Scanner(file)) {
            InputReader inputReader = new InputReader(scriptScanner, false);

            while (scriptScanner.hasNextLine()) {
                String line = scriptScanner.nextLine().trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                System.out.println("Выполнение команды: " + line);

                String[] parts = line.split("\\s+", 2);
                String commandName = parts[0];
                String argument = parts.length > 1 ? parts[1] : "";

                Request request = new Request(commandName, argument, null, client.ClientApp.username, client.ClientApp.password);

                if (commandName.equals("insert") || commandName.equals("add") || commandName.equals("update") || commandName.equals("remove_lower")) {
                    try {
                        StudyGroup group = inputReader.readStudyGroup();

                        request = new Request(commandName, argument, group, client.ClientApp.username, client.ClientApp.password);

                    } catch (Exception e) {
                        System.out.println("Ошибка при чтении объекта для команды " + commandName + ": " + e.getMessage());
                        continue;
                    }
                }

                Response response = invoker.handle(request);
                System.out.println(response.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ошибка: файл не найден: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении скрипта: " + e.getMessage());
        } finally {
            scriptStack.remove(file.getAbsolutePath());
        }
    }
}