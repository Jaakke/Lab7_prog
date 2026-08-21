package commands;

import collection.CollectionManager;
import network.Request;
import network.Response;

public class InfoCommand implements Command {
    private final CollectionManager collectionManager;

    public InfoCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        return new Response(collectionManager.getInfo());
    }

    @Override
    public String getDescription() {
        return "вывести информацию о коллекции";
    }

    @Override
    public String getName() {
        return "info";
    }
}