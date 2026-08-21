package commands;

import collection.CollectionManager;
import database.DatabaseManager;
import network.Request;
import network.Response;

import java.util.List;
import java.util.Map;

public class RemoveLowerKeyCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public RemoveLowerKeyCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public Response execute(Request request) {
        String arg = request.getArgument();
        if (arg == null || arg.trim().isEmpty()) {
            return new Response("Ошибка: Команда требует числовой аргумент (ключ).");
        }

        try {
            long targetKey = Long.parseLong(arg.trim());
            String username = request.getUsername();

            List<Long> keysToRemove = collectionManager.getCollection().entrySet().stream()
                    .filter(entry -> entry.getKey() < targetKey)
                    .filter(entry -> entry.getValue().getOwner() != null && entry.getValue().getOwner().equals(username))
                    .map(Map.Entry::getKey)
                    .toList();

            if (keysToRemove.isEmpty()) {
                return new Response("Не найдено принадлежащих вам элементов с ключом меньше " + targetKey + ".");
            }

            int count = 0;
            for (Long key : keysToRemove) {
                if (databaseManager.deleteGroup(key, username)) {
                    collectionManager.removeKey(key);
                    count++;
                }
            }

            return new Response("Успешно удалено ваших элементов: " + count);
        } catch (NumberFormatException e) {
            return new Response("Ошибка: Ключ должен быть целым числом.");
        } catch (Exception e) {
            return new Response("Ошибка при удалении элементов: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "удалить из коллекции все элементы, ключ которых меньше, чем заданный (только свои)";
    }

    @Override
    public String getName() {
        return "remove_lower_key";
    }
}