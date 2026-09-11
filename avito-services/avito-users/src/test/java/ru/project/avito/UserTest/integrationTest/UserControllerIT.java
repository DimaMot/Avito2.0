package ru.project.avito.UserTest.integrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.project.user.UserApplication;
import ru.project.user.dao.UserRepository;
import ru.project.user.dto.CreateUserDto;
import ru.project.user.feign.ItemClientForUsers;
import ru.project.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest(classes = UserApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты UserController")
public class UserControllerIT extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClientForUsers itemClient;

    @Test
    @DisplayName("Успешное создание пользователя")
    void createUser() throws Exception {
        CreateUserDto createUser = new CreateUserDto("name", "email@mail.ru");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value( "email@mail.ru"));

        assertEquals(1, userRepository.count());
        assertNotNull(userRepository.findAll().getFirst().getId());
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя")
    void deleteUserIfNotExist() throws Exception {
        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
        assertEquals(0,userRepository.count());
    }
}
