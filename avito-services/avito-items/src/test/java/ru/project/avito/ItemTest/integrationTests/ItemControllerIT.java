package ru.project.avito.ItemTest.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.project.item.ItemApplication;
import ru.project.item.dao.ItemRepository;
import ru.project.item.dto.ItemCreatDto;
import ru.project.item.dto.UserDto;
import ru.project.item.feign.BookingClient;
import ru.project.item.feign.UserForItemClient;
import ru.project.item.model.Item;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest(classes = ItemApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты ItemController")
public class ItemControllerIT extends BaseIntegrationTest {
    @Autowired
    private ItemRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserForItemClient userClient;

    @MockBean
    private BookingClient bookingClient;

    private static final String USER_HEADER = "X-Avito-User-Id";

    private final long testUserId = 1L;

    @Test
    @DisplayName("Успешное создание пользователя")
    void createItem() throws Exception {
        ItemCreatDto itemCreatDto = new ItemCreatDto("дрель", "хорошая дрель", true);
        UserDto user = new UserDto(testUserId, "dima", "dima@mail.ru");

        when(userClient.getUserById(testUserId)).thenReturn(user);

        mockMvc.perform(post("/items")
                    .header(USER_HEADER, testUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(itemCreatDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("дрель"))
                .andExpect(jsonPath("$.description").value("хорошая дрель"))
                .andExpect(jsonPath("$.available").value(true));

        assertEquals(1, repository.count());
        verify(userClient, times(1)).getUserById(testUserId);
    }

    @Test
    @DisplayName("Попытка получения несуществующего предмета")
    void getItemWhichDoesNotExist() throws Exception {
        mockMvc.perform(get("/items/999")
                    .header(USER_HEADER, testUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение вещей полльзователя")
    void getOwnerItems() throws Exception {
        repository.saveAndFlush(new Item(1L, "дрель", "крутая", true, testUserId, null, new ArrayList<>()));
        when(bookingClient.getAllBookingForItems(anyList())).thenReturn(Map.of());
        mockMvc.perform(get("/items")
                        .header(USER_HEADER, testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("дрель"));
    }
}
