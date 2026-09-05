package ru.project.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.project.item.dto.ItemDto;
import ru.project.item.dto.ItemDtoForBookingService;
import ru.project.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("""
    SELECT new ru.project.item.dto.ItemDto(i.id, i.name, i.description, i.available)
    FROM Item i
    WHERE i.available = true
    AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%'))
    OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))
    """)
    List<ItemDto> findAllBySearching(@Param("text") String text);

    @Query("""
    SELECT DISTINCT i
    FROM Item i
    LEFT JOIN FETCH i.comments
    WHERE i.ownerId = :ownerIds
    """)
    List<Item> findAllItemsWithComments(@Param("ownerIds") long ownerIds);

    @Query("""
    SELECT new ru.project.item.dto.ItemDtoForBookingService(i.id, i.name, i.description, i.available, i.ownerId)
    FROM Item i
    WHERE i.id = :itemId
    """)
    Optional<ItemDtoForBookingService> findItemById(@Param("itemId") long itemId);

    @Query("""
    SELECT new ru.project.item.dto.ItemDtoForBookingService(i.id, i.name, i.description, i.available, i.ownerId)
    FROM Item i
    WHERE i.id IN :itemIds
    """)
    List<ItemDtoForBookingService> findItemsByIds(@Param("itemIds") List<Long> itemIds);

    @Query("""
    SELECT i.id
    FROM Item i
    WHERE i.ownerId = :userId
    """)
    List<Long> findItemByUserId(@Param("userId") long userId);

    void deleteAllByOwnerId(long ownerId);
}
