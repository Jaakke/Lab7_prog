package commands;

import collection.CollectionManager;
import database.DatabaseManager;
import models.StudyGroup;
import network.Request;
import network.Response;

import java.util.List;
import java.util.Map;

public class RemoveLowerCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public RemoveLowerCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public Response execute(Request request) {
        if (!(request.getObjectArgument() instanceof StudyGroup)) {
            return new Response("Ошибка: Для выполнения команды требуется передать объект группы.");
        }

        StudyGroup compareGroup = (StudyGroup) request.getObjectArgument();
        String username = request.getUsername();

        try {
            List<Long> keysToRemove = collectionManager.getCollection().entrySet().stream()
                    .filter(entry -> entry.getValue().compareTo(compareGroup) < 0)
                    .filter(entry -> entry.getValue().getOwner() != null && entry.getValue().getOwner().equals(username))
                    .map(Map.Entry::getKey)
                    .toList();

            if (keysToRemove.isEmpty()) {
                return new Response("Не найдено принадлежащих вам элементов, меньших чем заданный.");
            }

            int count = 0;
            for (Long key : keysToRemove) {
                if (databaseManager.deleteGroup(key, username)) {
                    collectionManager.removeKey(key);
                    count++;
                }
            }

            return new Response("Успешно удалено ваших элементов: " + count);
        } catch (Exception e) {
            return new Response("Ошибка при удалении элементов: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "удалить из коллекции все элементы, меньшие, чем заданный (только свои)";
    }

    @Override
    public String getName() {
        return "remove_lower";
    }
}