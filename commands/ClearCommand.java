package commands;

import collection.CollectionManager;
import database.DatabaseManager;
import network.Request;
import network.Response;

public class ClearCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public ClearCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            String username = request.getUsername();

            databaseManager.clearUserGroups(username);

            String ramResult = collectionManager.clear(username);

            return new Response(ramResult);
        } catch (Exception e) {
            return new Response("Ошибка очистки: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() { return "очистить коллекцию"; }

    @Override
    public String getName() {
        return "clear";
    }
}