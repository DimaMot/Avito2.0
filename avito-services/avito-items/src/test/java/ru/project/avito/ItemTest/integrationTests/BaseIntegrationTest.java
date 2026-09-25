package ru.project.avito.ItemTest.integrationTests;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.project.item.ItemApplication;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(classes = ItemApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
public abstract class BaseIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16.1")
            .withDatabaseName("item-test")
            .withUsername("itemTest")
            .withPassword("12345");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.cloud.config.enabled", () -> "false");
    }
}
