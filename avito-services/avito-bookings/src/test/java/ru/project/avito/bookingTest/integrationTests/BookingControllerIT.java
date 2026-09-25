package ru.project.avito.bookingTest.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.project.booking.dao.BookingRepository;
import ru.project.booking.dto.BookingCreateDto;
import ru.project.booking.dto.ItemDto;
import ru.project.booking.dto.UserDto;
import ru.project.booking.feign.ItemForBookingClient;
import ru.project.booking.feign.UserForBookingClient;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты для BookingController")
public class BookingControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemForBookingClient itemClient;

    @MockBean
    private UserForBookingClient userClient;

    private static final String USER_HEADER = "X-Avito-User-Id";
    private final long testItemId = 1L;
    private final long testUserId = 2L;

    private ItemDto defaultItem;
    private UserDto defaultUser;

    @BeforeEach
    void setUpContext() {
        defaultItem = new ItemDto(testItemId, "Дрель", "Хорошая дрель", true, 1L);
        defaultUser = new UserDto(testUserId, "dima", "dima@mail.ru");
    }

    @Test
    @DisplayName("Успешное создание бронирования")
    void createBooking_WebFlow_Success() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        BookingCreateDto requestBody = new BookingCreateDto(testItemId, now.plusMinutes(5), now.plusHours(1));

        when(userClient.getUserById(testUserId)).thenReturn(defaultUser);
        when(itemClient.getItemById(testItemId)).thenReturn(defaultItem);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.name").value("Дрель"));

        assertEquals(1, bookingRepository.count());
        verify(userClient, times(1)).getUserById(testUserId);
        verify(itemClient, times(1)).getItemById(testItemId);
    }

    @Test
    @DisplayName("Попытка запросить несуществующую бронь возвращает 404")
    void getBooking_WhenDoesNotExist_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/bookings/9999")
                        .header(USER_HEADER, testUserId))
                .andExpect(status().isNotFound());
    }
}
