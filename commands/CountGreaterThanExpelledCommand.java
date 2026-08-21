package commands;

import collection.CollectionManager;
import network.Request;
import network.Response;

public class CountGreaterThanExpelledCommand implements Command {
    private final CollectionManager collectionManager;

    public CountGreaterThanExpelledCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        String arg = request.getArgument();
        if (arg == null || arg.trim().isEmpty()) {
            return new Response("Ошибка: Команда требует числовой аргумент (количество отчисленных студентов).");
        }

        try {
            long expelledThreshold = Long.parseLong(arg.trim());

            long count = collectionManager.getCollection().values().stream()
                    .filter(group -> group.getExpelledStudents() != null && group.getExpelledStudents() > expelledThreshold)
                    .count();

            return new Response("Количество элементов, у которых expelledStudents больше " + expelledThreshold + ": " + count);
        } catch (NumberFormatException e) {
            return new Response("Ошибка: Число отчисленных студентов должно быть целым числом.");
        } catch (Exception e) {
            return new Response("Ошибка при подсчете элементов: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "вывести количество элементов, значение поля expelledStudents которых больше заданного";
    }

    @Override
    public String getName() {
        return "count_greater_than_expelled_students";
    }
}