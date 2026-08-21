package commands;

import network.Request;
import network.Response;

public class ExitCommand implements Command {

    @Override
    public Response execute(Request request) {
        return new Response("Завершение работы клиента.");
    }

    @Override
    public String getDescription() {
        return "завершить программу (без сохранения в файл)";
    }

    @Override
    public String getName() {
        return "exit";
    }
}