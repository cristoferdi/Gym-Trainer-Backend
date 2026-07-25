package com.softech.entrenaback.routine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softech.entrenaback.auth.JwtService;
import com.softech.entrenaback.routine.dto.CreateRoutineRequest;
import com.softech.entrenaback.routine.dto.ExerciseBlockDto;
import com.softech.entrenaback.routine.dto.RoutineDayDto;
import com.softech.entrenaback.routine.dto.SubExerciseDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Sql(scripts = "/student-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RoutineControllerTest {

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
    private RoutineRepository routineRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        tokenA = "Bearer " + jwtService.generateToken("trainerA@test.com");
        tokenB = "Bearer " + jwtService.generateToken("trainerB@test.com");
    }

    private String extractId(String json) throws Exception {
        return objectMapper.readTree(json).get("id").asText();
    }

    private CreateRoutineRequest buildRequest(String name, int dayCount) {
        var request = new CreateRoutineRequest();
        request.setName(name);
        request.setDescription(null);
        var days = new java.util.ArrayList<RoutineDayDto>();
        for (int d = 0; d < dayCount; d++) {
            var day = new RoutineDayDto();
            day.setDayName("Day " + (d + 1));
            day.setOrderIndex(d);
            var blocks = new java.util.ArrayList<ExerciseBlockDto>();
            var block = new ExerciseBlockDto();
            block.setCombined(false);
            block.setSeries(3);
            block.setReps(12);
            block.setRestTime("60s");
            var sub = new SubExerciseDto();
            sub.setExerciseId("ex-001");
            sub.setName("Press Banca");
            sub.setGifUrl("http://test.com/press.gif");
            sub.setVideoUrl("http://test.com/press.mp4");
            sub.setInstructions(List.of("Bajar lento", "Subir explosivo"));
            block.setSubExercises(List.of(sub));
            blocks.add(block);
            day.setBlocks(blocks);
            days.add(day);
        }
        request.setDays(days);
        return request;
    }

    @Test
    void list_ShouldReturnOnlyOwnRoutines() throws Exception {
        var routineB = buildRequest("Rutina de B", 1);
        mockMvc.perform(post("/routines").header("Authorization", tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(routineB)))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/routines").header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_ShouldReject_WhenTrainerATriesToReadRoutineOfB() throws Exception {
        var routineB = buildRequest("Rutina de B", 1);
        var json = mockMvc.perform(post("/routines").header("Authorization", tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(routineB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var id = extractId(json);
        mockMvc.perform(get("/routines/{id}", id).header("Authorization", tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_ShouldReject_WhenTrainerATriesToUpdateRoutineOfB() throws Exception {
        var routineB = buildRequest("Rutina de B", 1);
        var json = mockMvc.perform(post("/routines").header("Authorization", tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(routineB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var id = extractId(json);
        var update = buildRequest("Hacked", 1);
        mockMvc.perform(put("/routines/{id}", id).header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldReject_WhenTrainerATriesToDeleteRoutineOfB() throws Exception {
        var routineB = buildRequest("Rutina de B", 1);
        var json = mockMvc.perform(post("/routines").header("Authorization", tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(routineB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var id = extractId(json);
        mockMvc.perform(delete("/routines/{id}", id).header("Authorization", tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn201AndPersistNestedStructure() throws Exception {
        var request = buildRequest("Full Routine", 2);
        var jsonResponse = mockMvc.perform(post("/routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("Full Routine"))
                .andExpect(jsonPath("$.days.length()").value(2))
                .andExpect(jsonPath("$.days[0].day_name").value("Day 1"))
                .andExpect(jsonPath("$.days[0].blocks.length()").value(1))
                .andExpect(jsonPath("$.days[0].blocks[0].series").value(3))
                .andExpect(jsonPath("$.days[0].blocks[0].sub_exercises.length()").value(1))
                .andExpect(jsonPath("$.days[1].day_name").value("Day 2"))
                .andReturn().getResponse().getContentAsString();
        var id = extractId(jsonResponse);

        entityManager.flush();
        entityManager.clear();

        var rutinaGuardada = routineRepository.findById(id).orElseThrow();
        assertEquals("Full Routine", rutinaGuardada.getName(), "El nombre no coincide en BD");
        assertEquals(2, rutinaGuardada.getDays().size(), "No se guardaron los 2 días en cascada");

        var primerDia = rutinaGuardada.getDays().get(0);
        assertEquals("Day 1", primerDia.getName());
        assertEquals(1, primerDia.getBlocks().size(), "No se guardó el bloque del día");

        var primerBloque = primerDia.getBlocks().get(0);
        assertEquals(3, primerBloque.getSeries());
        assertNotNull(primerBloque.getSubExercises());
        assertEquals(1, primerBloque.getSubExercises().size());
    }

    @Test
    void getById_ShouldReturnFullNestedStructure() throws Exception {
        var request = buildRequest("Detail Test", 1);
        var createJson = mockMvc.perform(post("/routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var id = extractId(createJson);
        mockMvc.perform(get("/routines/{id}", id).header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Detail Test"))
                .andExpect(jsonPath("$.days[0].day_name").value("Day 1"))
                .andExpect(jsonPath("$.days[0].blocks[0].series").value(3))
                .andExpect(jsonPath("$.days[0].blocks[0].sub_exercises[0].name").value("Press Banca"))
                .andExpect(jsonPath("$.coachName").isString());
    }

    @Test
    void update_ShouldAllowStructuralChangesWithOrphanRemoval() throws Exception {
        var request = buildRequest("Original", 2);
        var createJson = mockMvc.perform(post("/routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var id = extractId(createJson);
        var updated = buildRequest("Renamed", 1);
        mockMvc.perform(put("/routines/{id}", id).header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.days.length()").value(1));

        entityManager.flush();
        entityManager.clear();

        var rutinaActualizada = routineRepository.findById(id).orElseThrow();
        assertEquals("Renamed", rutinaActualizada.getName(), "El nombre no se actualizó en BD");
        assertEquals(1, rutinaActualizada.getDays().size(), "El día eliminado no fue borrado por orphan removal");
    }

    @Test
    void delete_ShouldCascadeDeleteDaysAndBlocks() throws Exception {
        var request = buildRequest("To Delete", 2);
        var createJson = mockMvc.perform(post("/routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var id = extractId(createJson);
        mockMvc.perform(delete("/routines/{id}", id).header("Authorization", tokenA))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertTrue(!routineRepository.existsById(id), "La rutina madre todavía existe en la BD");
    }

    @Test
    void anyEndpoint_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/routines")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/routines").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/routines/some-id").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/routines/some-id")).andExpect(status().isUnauthorized());
    }

    @Test
    void create_ShouldReturn400_WhenNameIsBlank() throws Exception {
        var request = new CreateRoutineRequest();
        request.setName("");
        request.setDays(List.of(new RoutineDayDto()));
        mockMvc.perform(post("/routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturn400_WhenDaysIsEmpty() throws Exception {
        var request = new CreateRoutineRequest();
        request.setName("No Days");
        request.setDays(List.of());
        mockMvc.perform(post("/routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}