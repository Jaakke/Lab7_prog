package commands;

import collection.CollectionManager;
import models.StudyGroup;
import network.Request;
import network.Response;

import java.util.List;

public class FilterStartsWithNameCommand implements Command {
    private final CollectionManager collectionManager;

    public FilterStartsWithNameCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        String prefix = request.getArgument();
        if (prefix == null || prefix.trim().isEmpty()) {
            return new Response("Ошибка: Необходимо передать подстроку для поиска (префикс).");
        }

        String searchPrefix = prefix.trim();
        List<StudyGroup> matchedGroups = collectionManager.getCollection().values().stream()
                .filter(group -> group.getName() != null && group.getName().startsWith(searchPrefix))
                .toList();

        if (matchedGroups.isEmpty()) {
            return new Response("Элементы, имя которых начинается с '" + searchPrefix + "', не найдены.");
        }

        StringBuilder result = new StringBuilder("Найденные элементы:\n");
        for (StudyGroup group : matchedGroups) {
            result.append(group.toString()).append("\n");
        }

        return new Response(result.toString().trim());
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение поля name которых начинается с заданной подстроки";
    }

    @Override
    public String getName() {
        return "filter_starts_with_name";
    }
}