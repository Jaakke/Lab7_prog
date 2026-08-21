package commands;

import network.Request;
import network.Response;

public class ExecuteScriptCommand implements Command {

    @Override
    public Response execute(Request request) {
        String arg = request.getArgument();
        if (arg == null || arg.trim().isEmpty()) {
            return new Response("Ошибка: Команда требует имя файла скрипта.");
        }

        return new Response("Выполнение скрипта из файла '" + arg.trim() + "' завершено.");
    }

    @Override
    public String getDescription() {
        return "считать и исполнить скрипт из указанного файла";
    }

    @Override
    public String getName() {
        return "execute_script";
    }
}