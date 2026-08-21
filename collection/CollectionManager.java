package collection;

import models.StudyGroup;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class CollectionManager {
    private LinkedHashMap<Long, StudyGroup> collection = new LinkedHashMap<>();
    private final ZonedDateTime creationDate;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public CollectionManager() {
        this.creationDate = ZonedDateTime.now();
    }

    public LinkedHashMap<Long, StudyGroup> getCollection() {
        return collection;
    }

    public void setCollection(LinkedHashMap<Long, StudyGroup> collection) {
        lock.writeLock().lock();
        try {
            if (collection != null) {
                this.collection = collection;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean checkId(Long id) {
        if (id == null) return false;
        lock.readLock().lock();
        try {
            return collection.values().stream()
                    .anyMatch(group -> id.equals(group.getId()));
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addWithId(Long id, StudyGroup group) {
        lock.writeLock().lock();
        try {
            group.setId(id);
            collection.put(id, group);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String insert(Long key, StudyGroup group) {
        lock.writeLock().lock();
        try {
            if (collection.containsKey(key)) {
                return "Ошибка: Ключ " + key + " уже существует в коллекции.";
            }
            collection.put(key, group);
            return "Элемент успешно добавлен в оперативную память.";
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String update(Long id, StudyGroup newGroup) {
        lock.writeLock().lock();
        try {

            Long key = collection.entrySet().stream()
                    .filter(entry -> entry.getValue().getId().equals(id))
                    .map(java.util.Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (key == null) {
                return "Элемент с ID " + id + " не найден в оперативной памяти.";
            }

            StudyGroup oldGroup = collection.get(key);
            newGroup.setId(id);
            if (oldGroup != null && newGroup.getOwner() == null) {
                newGroup.setOwner(oldGroup.getOwner());
            }

            collection.put(key, newGroup);
            return "Элемент с ID " + id + " успешно обновлен в оперативной памяти.";
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String removeKey(Long key) {
        lock.writeLock().lock();
        try {
            if (collection.remove(key) != null) {
                return "Элемент с ключом " + key + " удален из оперативной памяти.";
            }
            return "Элемент с таким ключом не найден.";
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String clear(String username) {
        lock.writeLock().lock();
        try {
            collection.entrySet().removeIf(entry ->
                    entry.getValue().getOwner() != null && entry.getValue().getOwner().equals(username)
            );
            return "Ваши элементы успешно удалены из оперативной памяти.";
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getInfo() {
        lock.readLock().lock();
        try {
            return "Тип коллекции: LinkedHashMap\n" +
                    "Дата инициализации: " + creationDate + "\n" +
                    "Количество элементов: " + collection.size() + "\n" +
                    "Минимальный ID: " + getMinId() + "\n" +
                    "Максимальный ID: " + getMaxId();
        } finally {
            lock.readLock().unlock();
        }
    }

    public String show() {
        lock.readLock().lock();
        try {
            if (collection.isEmpty()) {
                return "Коллекция пуста.";
            }
            return collection.values().stream()
                    .map(StudyGroup::toString)
                    .collect(Collectors.joining("\n"));
        } finally {
            lock.readLock().unlock();
        }
    }

    private Long getMinId() {
        return collection.values().stream()
                .mapToLong(StudyGroup::getId)
                .min()
                .orElse(0L);
    }

    private Long getMaxId() {
        return collection.values().stream()
                .mapToLong(StudyGroup::getId)
                .max()
                .orElse(0L);
    }
}