package commands;

import network.Request;
import network.Response;

import java.util.Map;

public class HelpCommand implements Command {
    private final Map<String, Command> commands;
    public HelpCommand(Map<String, Command> commands) {
        this.commands = commands;
    }

    @Override
    public Response execute(Request request) {
        if (commands == null || commands.isEmpty()) {
            return new Response("Список команд пуст или не инициализирован.");
        }

        StringBuilder builder = new StringBuilder("Список доступных команд:\n");
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            builder.append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue().getDescription())
                    .append("\n");
        }

        return new Response(builder.toString().trim());
    }

    @Override
    public String getDescription() {
        return "вывести справку по доступным командам";
    }

    @Override
    public String getName() {
        return "help";
    }
}