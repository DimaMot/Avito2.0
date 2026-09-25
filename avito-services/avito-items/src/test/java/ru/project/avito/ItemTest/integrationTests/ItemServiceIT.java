package ru.project.avito.ItemTest.integrationTests;


import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;
import ru.project.item.dao.ItemRepository;
import ru.project.item.dto.*;
import ru.project.item.dto.mapper.CommentMapper;
import ru.project.item.dto.mapper.ItemMapper;
import ru.project.item.exceptions.NotFoundException;
import ru.project.item.feign.UserForItemClient;
import ru.project.item.model.Item;
import ru.project.item.service.ItemService;
import ru.project.item.service.update.ItemFieldUpdate;
import ru.project.item.service.validate.ItemValidation;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Интеграционные тесты ItemService")
public class ItemServiceIT extends BaseIntegrationTest {

    @Autowired
    private ItemService itemService;

    @MockBean
    private UserForItemClient userClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private CommentMapper commentMapper;

    @SpyBean
    private Clock clock;

    @SpyBean
    private ItemValidation itemValidation;

    @Autowired
    private List<ItemFieldUpdate> fieldUpdates;

    @Autowired
    private EntityManager entityManager;

    private final long testUserId = 1L;

    @Test
    @DisplayName("Успешное создание предмета")
    void createItem() {
        ItemCreatDto createNewItem = new ItemCreatDto("newItem", "newDescription", true);
        UserDto testUser = new UserDto(testUserId, "testName", "test@mail.ru");
        when(userClient.getUserById(testUserId)).thenReturn(testUser);

        ItemDto item = itemService.creatItem(createNewItem, testUserId);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, itemRepository.count());
        assertNotNull(item);
        assertEquals(createNewItem.name(), item.name());

        verify(userClient, times(1)).getUserById(testUserId);
    }

    @Test
    @DisplayName("Успешное получение предмета по id")
    void getItemById() {
        Item item = itemRepository.saveAndFlush(new Item(null, "item", "discription", true, testUserId, null, new ArrayList<>()));
        entityManager.clear();

        ItemDtoForBookingService createdItem = itemService.getItemByIdForBooking(item.getId());
        assertEquals(createdItem.description(), item.getDescription());
        assertEquals(createdItem.available(), item.getAvailable());
    }

    @Test
    @DisplayName("Успешное обнолвение предмета")
    void updateItem() {
        Item item = itemRepository.saveAndFlush(new Item(null, "item", "discription", true, testUserId, null, new ArrayList<>()));
        entityManager.clear();

        ItemDto updateItem = new ItemDto(item.getId(), "newItemName", null, null);
        ItemDto updated = itemService.updateItem(item.getId(), updateItem, testUserId);
        entityManager.flush();
        entityManager.clear();

        assertEquals("newItemName", updated.name());
        assertEquals(item.getId(), updated.id());
        assertEquals(item.getDescription(), updated.description());
        assertEquals(item.getAvailable(), updated.available());
    }

    @Test
    @DisplayName("Успешное удаление предмета")
    void deleteItem() {
        Item item = itemRepository.saveAndFlush(new Item(null, "item", "discription", true, testUserId, null, new ArrayList<>()));
        entityManager.clear();

        itemService.deleteItemById(item.getId());
        entityManager.flush();
        entityManager.clear();
        assertEquals(0, itemRepository.count());
    }

    @Test
    @DisplayName("Успешный запрет на обновлени не своей вещи")
    void notUpdatedItem() {
        Item item = itemRepository.saveAndFlush(new Item(null, "item", "discription", true, testUserId, null, new ArrayList<>()));
        entityManager.clear();

        ItemDto updateItem = new ItemDto(item.getId(), "newItemName", null, null);
        assertThrows(NotFoundException.class, () -> itemService.updateItem(item.getId(), updateItem, 999L));
    }

    @Test
    @DisplayName("Создание предмета у несущесвтующего пользователя")
    void createItemFail() {
        ItemCreatDto createNewItem = new ItemCreatDto("newItem", "newDescription", true);

        Request request = Request.create(Request.HttpMethod.GET, "users/1", new HashMap<>(), null, new RequestTemplate());
        FeignException feignException = new FeignException.NotFound("Пользователь не найден", request, null, null);

        when(userClient.getUserById(10L)).thenThrow(feignException);

        assertThrows(NotFoundException.class, () -> itemService.creatItem(createNewItem, 10L));
        assertEquals(0, itemRepository.count());
    }

    @Test
    @DisplayName("Успешное создание коммента")
    void createComment() {
        Item item = itemRepository.saveAndFlush(new Item(null, "item", "discription", true, testUserId, null, new ArrayList<>()));
        entityManager.clear();

        CommentCreateDto createDto = new CommentCreateDto("Good thing");
        UserDto user = new UserDto(testUserId, "Dima", "Dima@mail.ru");

        when(userClient.getUserById(testUserId)).thenReturn(user);
        doNothing().when(itemValidation).verifyBookingExist(eq(testUserId), eq(item.getId()), any());

        CommentDto result = itemService.writeAComment(createDto, testUserId, item.getId());

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(createDto.text(), result.text());
        assertEquals("Dima", result.authorName());
    }
}
