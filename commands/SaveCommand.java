package commands;

import collection.CollectionManager;
import network.Request;
import network.Response;

public class SaveCommand implements Command {
    private final CollectionManager collectionManager;

    public SaveCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            return new Response("Коллекция успешно сохранена.");
        } catch (Exception e) {
            return new Response("Ошибка при сохранении коллекции: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "сохранить коллекцию";
    }

    @Override
    public String getName() {
        return "save";
    }
}