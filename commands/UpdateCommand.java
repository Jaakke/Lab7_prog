package commands;

import collection.CollectionManager;
import database.DatabaseManager;
import models.StudyGroup;
import network.Request;
import network.Response;

public class UpdateCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public UpdateCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public Response execute(Request request) {
        String arg = request.getArgument();
        if (arg == null || arg.trim().isEmpty()) {
            return new Response("Ошибка: Команда требует ID.");
        }

        try {
            long id = Long.parseLong(arg.trim());

            if (!collectionManager.checkId(id)) {
                return new Response("Ошибка: Элемент с ID " + id + " не найден.");
            }

            StudyGroup existingGroup = collectionManager.getCollection().get(id);
            if (existingGroup != null && request.getUsername() != null && !request.getUsername().equals(existingGroup.getOwner())) {
                return new Response("Ошибка: Элемент с ID " + id + " вам не принадлежит.");
            }

            if (request.getObjectArgument() == null) {
                return new Response("READY");
            }

            StudyGroup newGroup = (StudyGroup) request.getObjectArgument();
            newGroup.setId(id);

            databaseManager.updateGroup(id, newGroup);
            collectionManager.update(id, newGroup);

            return new Response("Элемент с ID " + id + " успешно обновлен.");

        } catch (NumberFormatException e) {
            return new Response("Ошибка: ID должен быть целым числом.");
        } catch (Exception e) {
            return new Response("Ошибка при обновлении: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "обновить значение элемента коллекции, id которого равен заданному";
    }

    @Override
    public String getName() {
        return "update";
    }
}