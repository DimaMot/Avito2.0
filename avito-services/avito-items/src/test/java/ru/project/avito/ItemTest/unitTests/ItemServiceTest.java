package ru.project.avito.ItemTest.unitTests;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.project.item.dao.CommentRepository;
import ru.project.item.dao.ItemRepository;
import ru.project.item.dto.*;
import ru.project.item.dto.mapper.CommentMapper;
import ru.project.item.dto.mapper.ItemMapper;
import ru.project.item.exceptions.NotFoundException;
import ru.project.item.feign.BookingClient;
import ru.project.item.feign.UserForItemClient;
import ru.project.item.model.Comment;
import ru.project.item.model.Item;
import ru.project.item.service.ItemServiceImpl;
import ru.project.item.service.update.ItemFieldUpdate;
import ru.project.item.service.validate.ItemValidation;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ItemServiceImpl")
public class ItemServiceTest {
    private final long testUserId = 10;
    private final long testItemId = 1;
    private final String testUserName = "testUserName";
    private final String testUserEmail = "testUserEmail";
    private final String testItemName = "testItemName";
    private final String testItemDescription = "testDescription";
    private final boolean available = true;

    @Mock private ItemFieldUpdate availableFieldUpdate;
    @Mock private ItemFieldUpdate descriptionFieldUpdate;
    @Mock private ItemFieldUpdate nameFieldUpdate;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-09-05T12:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private BookingClient bookingClient;

    @Spy
    private List<ItemFieldUpdate> fieldUpdates = new ArrayList<>();

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ItemValidation itemValidation;

    @Mock
    private UserForItemClient userForItemClient;

    @InjectMocks
    private ItemServiceImpl itemService;

    @BeforeEach
    void beforeEach() {
        fieldUpdates.clear();
        fieldUpdates.add(availableFieldUpdate);
        fieldUpdates.add(nameFieldUpdate);
        fieldUpdates.add(descriptionFieldUpdate);
    }

    @Test
    @DisplayName("Успешно создание предмета")
    void createItem_WhenUserExist() {
        ItemCreatDto createItem = new ItemCreatDto(testItemName, testItemDescription, available);
        UserDto testUser = new UserDto(testUserId, testUserName, testUserEmail);
        Item item = new Item(testItemId, testItemName, testItemDescription, available, testUserId, null, null);
        ItemDto expectedDto = new ItemDto(testItemId, testItemName, testItemDescription, available);

        when(userForItemClient.getUserById(testUserId)).thenReturn(testUser);
        when(itemMapper.toItem(createItem, testUserId)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(expectedDto);

        ItemDto result = itemService.creatItem(createItem, testUserId);

        assertEquals(result.id(), expectedDto.id());
        assertEquals(result.name(), expectedDto.name());
        assertEquals(result.description(), expectedDto.description());
        assertEquals(result.available(), expectedDto.available());

        verify(itemValidation, times(1)).initializeItem(createItem);
        verify(itemMapper, times(1)).toItem(createItem, testUserId);
        verify(itemMapper, times(1)).toItemDto(item);
        verify(userForItemClient, times(1)).getUserById(testUserId);
        verify(itemRepository, times(1)).save(item);

        verifyNoMoreInteractions(userForItemClient, itemMapper, itemRepository, itemValidation);
    }

    @Test
    @DisplayName("Вырос исключения при создания предмета с несущ. пользователем")
    void createItem_WhenUserDoesNotExist_ReturnTypeNotFoundEx() {
        ItemCreatDto createItem = new ItemCreatDto(testItemName, testItemDescription, available);

        Request request = Request.create(Request.HttpMethod.GET, "/users/" + testUserId, new HashMap<>(), null, new RequestTemplate());
        FeignException.NotFound feignException = new FeignException.NotFound("Пользователь не найден", request, null, null);

        when(userForItemClient.getUserById(testUserId)).thenThrow(feignException);

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.creatItem(createItem, testUserId));

        assertEquals("Пользователь не найден", exception.getMessage());

        verify(itemValidation, times(1)).initializeItem(createItem);
        verify(itemRepository, never()).save(any(Item.class));
        verify(userForItemClient, times(1)).getUserById(testUserId);

        verifyNoMoreInteractions(userForItemClient, itemValidation);
    }

    @Test
    @DisplayName("Успешное получение вещи от его владельца")
    void getItemById_WhenRequesterIsOwner_ReturnTypeDtoWithBookings() {
        Item item = new Item(testItemId, testItemName, testItemDescription, available, testUserId, null, new ArrayList<>());
        OffsetDateTime now = OffsetDateTime.now(clock);

        BookingShortDto prevBooking = new BookingShortDto(100, now.minusDays(2), now.minusDays(1), 5, testItemId);
        BookingShortDto nextBooking = new BookingShortDto(101, now.plusDays(2), now.plusDays(3), 6, testItemId);

        ItemWithBookingDto expectedDto = new ItemWithBookingDto(testItemId, testItemName, testItemDescription, available, new ArrayList<>(), prevBooking, nextBooking);

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(item));
        when(userForItemClient.getUsersByUserIds(anyList())).thenReturn(List.of());
        when(bookingClient.getAllBookingForItems(List.of(testItemId))).thenReturn(Map.of(testItemId, List.of(prevBooking, nextBooking)));
        when(itemMapper.toItemWithBookingDto(eq(item), anyList(), eq(prevBooking), eq(nextBooking), anyMap())).thenReturn(expectedDto);

        ItemWithBookingDto result = itemService.getItemById(testItemId, testUserId);

        assertNotNull(result);
        assertEquals(prevBooking, result.prevBooking());
        assertEquals(nextBooking, result.nextBooking());

        verify(itemRepository, times(1)).findById(testItemId);
        verify(bookingClient, times(1)).getAllBookingForItems(List.of(testItemId));
    }

    @Test
    @DisplayName("Выброс NotFoundException при получении вещи с несуществующим ID")
    void getItemById_WhenItemDoesNotExist_ReturnTypeNotFoundEx() {
        long invalidItemId = 999L;
        when(itemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(invalidItemId, testUserId));

        assertEquals("Предмет с id " + invalidItemId + " не найден", exception.getMessage());

        verify(itemRepository, times(1)).findById(invalidItemId);
        verifyNoInteractions(bookingClient, userForItemClient, itemMapper);
    }

    @Test
    @DisplayName("Успешное обновление полей предмета владельцем через пайплайн стратегий")
    void updateItem_WhenUserIsOwner_ShouldTriggerFieldUpdaters() {
        Item oldItem = new Item(testItemId, "oldName", "oldDesc", available, testUserId, null, null);
        ItemDto updateRequest = new ItemDto(testItemId, testItemName, testItemDescription, false);
        Item updatedItem = new Item(testItemId, testItemName, testItemDescription, false, testUserId, null, null);
        ItemDto expectedDto = new ItemDto(testItemId, testItemName, testItemDescription, false);

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(oldItem));
        when(itemRepository.saveAndFlush(oldItem)).thenReturn(updatedItem);
        when(itemMapper.toItemDto(updatedItem)).thenReturn(expectedDto);
        when(availableFieldUpdate.support(updateRequest)).thenReturn(true);

        ItemDto result = itemService.updateItem(testItemId, updateRequest, testUserId);

        assertNotNull(result);
        assertFalse(result.available());

        verify(availableFieldUpdate, times(1)).support(updateRequest);
        verify(availableFieldUpdate, times(1)).updateField(oldItem, updateRequest);
        verify(itemRepository, times(1)).saveAndFlush(oldItem);
    }

    @Test
    @DisplayName("Выброс NotFoundException при попытке обновить вещь чужим пользователем")
    void updateItem_WhenUserIsNotOwner_ReturnTypeNotFoundEx() {
        long hackerId = 20;
        Item oldItem = new Item(testItemId, testItemName, testItemDescription, available, testUserId, null, null);
        ItemDto updateRequest = new ItemDto(testItemId, testItemName, testItemDescription, false);

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(oldItem));

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(testItemId, updateRequest, hackerId));

        assertTrue(exception.getMessage().contains("не является собственником предмета"));

        verify(itemRepository, times(1)).findById(testItemId);
        verify(itemRepository, never()).saveAndFlush(any(Item.class));
        verifyNoInteractions(availableFieldUpdate, nameFieldUpdate, descriptionFieldUpdate);
    }

    @Test
    @DisplayName("Успешное добавление комментария к предмету")
    void writeAComment_Success_ShouldReturnCommentDto() {
        CommentCreateDto createCommentDto = new CommentCreateDto("Отличная дрель!");
        UserDto user = new UserDto(testUserId, testUserName, testUserEmail);
        Item item = new Item(testItemId, testItemName, testItemDescription, available, testUserId, null, new ArrayList<>());

        Comment comment = new Comment();
        comment.setId(50L);
        comment.setText("Отличная дрель!");

        CommentDto expected = new CommentDto(50L, "Отличная дрель!", testUserName, OffsetDateTime.now(clock));

        when(userForItemClient.getUserById(testUserId)).thenReturn(user);
        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(item));
        when(commentMapper.toComment(createCommentDto)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentDto(eq(comment), anyMap())).thenReturn(expected);

        CommentDto result = itemService.writeAComment(createCommentDto, testUserId, testItemId);

        assertNotNull(result);
        assertEquals("Отличная дрель!", result.text());
        assertEquals(testUserName, result.authorName());

        verify(itemValidation, times(1)).verifyBookingExist(eq(testUserId), eq(testItemId), any(OffsetDateTime.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Выброс NotFoundException при добавлении отзыва от несуществующего юзера")
    void writeAComment_WhenUserDoesNotExist_ThrowsNotFoundException() {
        CommentCreateDto createComment = new CommentCreateDto("Фейковый отзыв");
        long fakeUserId = 777L;

        Request request = Request.create(Request.HttpMethod.GET, "/users/" + fakeUserId, new HashMap<>(), null, new RequestTemplate());
        FeignException.NotFound feignException = new FeignException.NotFound("User not found", request, null, null);
        when(userForItemClient.getUserById(fakeUserId)).thenThrow(feignException);

        assertThrows(NotFoundException.class, () -> itemService.writeAComment(createComment, fakeUserId, testItemId));

        verify(userForItemClient, times(1)).getUserById(fakeUserId);
        verifyNoInteractions(itemRepository, commentRepository, itemValidation);
    }

    @Test
    @DisplayName("Выброс NotFoundException при попытке оставить отзыв на несуществующую вещь")
    void writeAComment_WhenItemDoesNotExist_ThrowsNotFoundException() {
        CommentCreateDto createComment = new CommentCreateDto("Отзыв в пустоту");
        long invalidItemId = 999L;
        UserDto mockUser = new UserDto(testUserId, testUserName, testUserEmail);

        when(userForItemClient.getUserById(testUserId)).thenReturn(mockUser);
        when(itemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.writeAComment(createComment, testUserId, invalidItemId));

        assertEquals("Предмет с id " + invalidItemId + " не найден", exception.getMessage());

        verify(userForItemClient, times(1)).getUserById(testUserId);
        verify(itemRepository, times(1)).findById(invalidItemId);
        verifyNoInteractions(commentRepository, itemValidation);
    }
}
