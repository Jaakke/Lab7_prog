package server;

import collection.CollectionManager;
import commands.*;
import database.DatabaseManager;
import network.Request;
import network.Response;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandManager(CollectionManager collectionManager, DatabaseManager databaseManager) {
        register(new ShowCommand(collectionManager));
        register(new InsertCommand(collectionManager, databaseManager));
        register(new SaveCommand(collectionManager));
        register(new UpdateCommand(collectionManager, databaseManager));
        register(new RemoveKeyCommand(collectionManager, databaseManager));
        register(new ClearCommand(collectionManager, databaseManager));
        register(new RemoveGreaterKeyCommand(collectionManager, databaseManager));
        register(new RemoveLowerCommand(collectionManager, databaseManager));
        register(new RemoveLowerKeyCommand(collectionManager, databaseManager));
        register(new FilterStartsWithNameCommand(collectionManager));
        register(new FilterContainsNameCommand(collectionManager));
        register(new CountGreaterThanExpelledCommand(collectionManager));
        register(new ExecuteScriptCommand());
        register(new ExitCommand());
    }

    public void register(Command command) {
        if (command != null && command.getName() != null) {
            commands.put(command.getName().toLowerCase(), command);
        }
    }

    public void register(String name, Command command) {
        if (name != null && command != null) {
            commands.put(name.toLowerCase(), command);
        }
    }

    public Response execute(Request request) {
        if (request == null || request.getCommandName() == null) {
            return new Response("Ошибка: Некорректный запрос команды.");
        }

        Command command = commands.get(request.getCommandName().toLowerCase());
        if (command == null) {
            return new Response("Команда не найдена.");
        }

        return command.execute(request);
    }

    public Map<String, Command> getCommands() {
        return commands;
    }
}