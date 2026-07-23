package com.softech.entrenaback.exercise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class ExerciseControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private MockMvc mockMvc;
    private String existingExerciseId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        var page = exerciseRepository.search(null, null, null, PageRequest.of(0, 1));
        if (!page.isEmpty()) {
            existingExerciseId = page.getContent().get(0).getId();
        }
    }

    @Test
    void list_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/exercises"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void list_ShouldReturn200With10Elements_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/exercises?page=1&limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(10));
    }

    @Test
    @WithMockUser
    void getById_ShouldReturn200_WhenExerciseExists() throws Exception {
        mockMvc.perform(get("/exercises/{id}", existingExerciseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(existingExerciseId));
    }

    @Test
    @WithMockUser
    void getById_ShouldReturn404_WhenExerciseDoesNotExist() throws Exception {
        mockMvc.perform(get("/exercises/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
