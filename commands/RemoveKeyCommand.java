package commands;

import collection.CollectionManager;
import database.DatabaseManager;
import network.Request;
import network.Response;

public class RemoveKeyCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public RemoveKeyCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public Response execute(Request request) {
        String arg = request.getArgument();
        if (arg == null || arg.trim().isEmpty()) {
            return new Response("Ошибка: Команда требует аргумент (ID элемента).");
        }

        try {
            long id = Long.parseLong(arg.trim());
            String username = request.getUsername();

            if (databaseManager.deleteGroup(id, username)) {

                String ramResult = collectionManager.removeKey(id);
                return new Response("Удаление из БД успешно. " + ramResult);
            } else {
                return new Response("Ошибка: Элемент с ID " + id + " вам не принадлежит или не существует.");
            }
        } catch (NumberFormatException e) {
            return new Response("Ошибка: ID должен быть целым числом.");
        } catch (Exception e) {
            return new Response("Ошибка удаления: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "удалить элемент из коллекции по его ключу/ID (только свой)";
    }

    @Override
    public String getName() {
        return "remove_key";
    }
}