package com.softech.entrenaback.customexercise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softech.entrenaback.auth.JwtService;
import com.softech.entrenaback.customexercise.dto.CustomExerciseRequest;
import com.softech.entrenaback.trainer.TrainerRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Sql(scripts = "/custom-exercise-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CustomExerciseControllerTest {

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
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomExerciseRepository customExerciseRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    private MockMvc mockMvc;
    private String tokenA;
    private String tokenB;
    private String exerciseAId;
    private String exerciseBId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        tokenA = "Bearer " + jwtService.generateToken("trainerA@test.com");
        tokenB = "Bearer " + jwtService.generateToken("trainerB@test.com");

        exerciseAId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        exerciseBId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    }

    private String extractId(String json) throws Exception {
        return objectMapper.readTree(json).get("id").asText();
    }

    private CustomExerciseRequest buildRequest(String name) {
        var request = new CustomExerciseRequest();
        request.setName(name);
        request.setMuscle("Pecho");
        request.setEquipment("Barra");
        request.setGifUrl("http://test.com/exercise.gif");
        request.setVideoUrl("http://test.com/exercise.mp4");
        request.setTarget("Pectorales");
        request.setSecondaryMuscles(List.of("Tríceps", "Delts"));
        request.setInstructions(List.of("Calentar", "Ejecutar con control"));
        return request;
    }

    @Test
    void list_ShouldReturnOnlyOwnExercises() throws Exception {
        mockMvc.perform(get("/custom-exercises").header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Press Banca A"))
                .andExpect(jsonPath("$[1].name").value("Remo A"))
                .andExpect(jsonPath("$[2]").doesNotExist());
    }

    @Test
    void getById_ShouldDenyAccessToOtherTrainerExercise() throws Exception {
        mockMvc.perform(get("/custom-exercises/{id}", exerciseBId).header("Authorization", tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ShouldDenyAccessToOtherTrainerExercise() throws Exception {
        var request = buildRequest("Hacked Exercise");
        mockMvc.perform(put("/custom-exercises/{id}", exerciseBId).header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_ShouldDenyAccessToOtherTrainerExercise() throws Exception {
        mockMvc.perform(delete("/custom-exercises/{id}", exerciseBId).header("Authorization", tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldPersistExerciseWithTrainerForeignKey() throws Exception {
        var request = buildRequest("Nuevo Ejercicio");
        var response = mockMvc.perform(post("/custom-exercises").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var id = extractId(response);

        entityManager.flush();
        entityManager.clear();

        var ce = customExerciseRepository.findById(id).orElseThrow();
        assertEquals("Nuevo Ejercicio", ce.getName());
        assertEquals("Pecho", ce.getMuscle());
        assertEquals("Barra", ce.getEquipment());
        assertEquals("http://test.com/exercise.gif", ce.getGifUrl());
        assertEquals("http://test.com/exercise.mp4", ce.getVideoUrl());
        assertEquals("Pectorales", ce.getTarget());
        assertNotNull(ce.getSecondaryMuscles());
        assertNotNull(ce.getInstructions());

        var trainer = trainerRepository.findByEmail("trainerA@test.com").orElseThrow();
        assertEquals(trainer.getId(), ce.getTrainer().getId());
    }

    @Test
    void update_ShouldModifyAndPersistChanges() throws Exception {
        var request = buildRequest("Press Banca Modificado");
        request.setMuscle("Pecho completo");
        request.setTarget("Pectorales mayores");

        mockMvc.perform(put("/custom-exercises/{id}", exerciseAId).header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Press Banca Modificado"))
                .andExpect(jsonPath("$.muscle").value("Pecho completo"))
                .andExpect(jsonPath("$.target").value("Pectorales mayores"));

        entityManager.flush();
        entityManager.clear();

        var ce = customExerciseRepository.findById(exerciseAId).orElseThrow();
        assertEquals("Press Banca Modificado", ce.getName());
        assertEquals("Pecho completo", ce.getMuscle());
        assertEquals("Pectorales mayores", ce.getTarget());
    }

    @Test
    void delete_ShouldRemoveExerciseFromDatabase() throws Exception {
        mockMvc.perform(delete("/custom-exercises/{id}", exerciseAId).header("Authorization", tokenA))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertFalse(customExerciseRepository.existsById(exerciseAId));
    }

    @Test
    void create_ShouldRejectBlankName() throws Exception {
        var request = buildRequest("");
        mockMvc.perform(post("/custom-exercises").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_ShouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/custom-exercises"))
                .andExpect(status().isUnauthorized());
    }
}
