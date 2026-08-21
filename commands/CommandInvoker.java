package commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import network.Request;
import network.Response;

public class CommandInvoker {
    private final Map<String, Command> commands = new HashMap<>();

    public Response handle(Request request) {
        String commandName = request.getCommandName().toLowerCase();
        Command command = commands.get(commandName);

        if (command == null) {
            return new Response("Ошибка: Команда '" + commandName + "' не найдена.");
        }

        return command.execute(request);
    }

    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }
}