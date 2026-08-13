package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
@Slf4j
public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long globalId = 0;

    public Optional<Item> getItemById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    public List<Item> getAllItemsFromUser(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .toList();
    }

    public Item createItem(Item item) {
        item.setId(getNextId());
        log.info("Предмету присвоен id {}", item.getId());
        items.put(item.getId(), item);
        return item;
    }

    public List<Item> search(String text) {
        return items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> (item.getDescription() != null && item.getDescription().toLowerCase().contains(text)) ||
                        (item.getName() != null && item.getName().toLowerCase().contains(text)))
                .toList();
    }

    public Item deleteItemById(long itemId) {
        return items.remove(itemId);
    }

    private synchronized long getNextId() {
        return ++globalId;
    }
}
