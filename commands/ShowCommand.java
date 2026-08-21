package commands;

import collection.CollectionManager;
import network.Request;
import network.Response;

public class ShowCommand implements Command {
    private final CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        String collectionContent = collectionManager.show();
        return new Response(collectionContent);
    }

    @Override
    public String getDescription() {
        return "вывести в стандартный вывод все элементы коллекции в строковом представлении";
    }

    @Override
    public String getName() {
        return "show";
    }
}