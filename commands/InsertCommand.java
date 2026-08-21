package commands;

import collection.CollectionManager;
import database.DatabaseManager;
import models.StudyGroup;
import network.Request;
import network.Response;

public class InsertCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public InsertCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public Response execute(Request request) {
        String arg = request.getArgument();
        if (arg == null || arg.trim().isEmpty()) {
            return new Response("Ошибка: Команда требует аргумент (ключ).");
        }

        if (!(request.getObjectArgument() instanceof StudyGroup)) {
            return new Response("Ошибка: Для выполнения команды требуется передать объект группы.");
        }

        try {
            long key = Long.parseLong(arg.trim());
            StudyGroup group = (StudyGroup) request.getObjectArgument();
            String username = request.getUsername();

            if (username != null) {
                group.setOwner(username);
            }

            long generatedId = databaseManager.insertGroup(group, username);
            if (generatedId == -1) {
                return new Response("Ошибка: Не удалось сохранить элемент в базу данных.");
            }
            group.setId(generatedId);

            String ramResult = collectionManager.insert(generatedId, group);

            return new Response("Элемент с ID " + generatedId + " успешно добавлен. " + ramResult);
        } catch (NumberFormatException e) {
            return new Response("Ошибка: Ключ должен быть целым числом.");
        } catch (Exception e) {
            return new Response("Ошибка добавления элемента: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент с заданным ключом";
    }

    @Override
    public String getName() {
        return "insert";
    }
}