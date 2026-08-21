package ru.project.avito.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.project.avito.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long userId);

    @Query("""
            SELECT i from Item i
            WHERE i.available = true
            AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%'))
            OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))
            """)
    List<Item> findAllBySearching(String text);
}
