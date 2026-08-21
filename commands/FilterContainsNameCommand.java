package commands;

import collection.CollectionManager;
import models.StudyGroup;
import network.Request;
import network.Response;
import java.util.stream.Collectors;

public class FilterContainsNameCommand implements Command {
    private final CollectionManager collectionManager;

    public FilterContainsNameCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        String substring = request.getArgument();

        if (substring == null || substring.trim().isEmpty()) {
            return new Response("Ошибка: Команда требует текстовый аргумент (подстроку для поиска).");
        }

        String result = collectionManager.getCollection().values().stream()
                .filter(group -> group.getName() != null && group.getName().contains(substring))
                .map(StudyGroup::toString)
                .collect(Collectors.joining("\n"));

        if (result.isEmpty()) {
            return new Response("Элементы, содержащие '" + substring + "' в названии, не найдены.");
        }

        return new Response(result);
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение поля name которых содержит заданную подстроку";
    }

    @Override
    public String getName() {
        return "filter_contains_name";
    }
}