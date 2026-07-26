package com.softech.entrenaback.assigned;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softech.entrenaback.assigned.dto.AssignRoutineRequest;
import com.softech.entrenaback.auth.JwtService;
import com.softech.entrenaback.routine.Routine;
import com.softech.entrenaback.routine.RoutineDay;
import com.softech.entrenaback.routine.ExerciseBlock;
import com.softech.entrenaback.routine.SubExerciseDetail;
import com.softech.entrenaback.routine.RoutineRepository;
import com.softech.entrenaback.student.Student;
import com.softech.entrenaback.student.StudentRepository;
import com.softech.entrenaback.trainer.Trainer;
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
@Sql(scripts = "/assigned-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AssignedRoutineControllerTest {

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
    private TrainerRepository trainerRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private AssignedRoutineRepository assignedRoutineRepository;

    private MockMvc mockMvc;
    private String tokenA;
    private String tokenB;
    private String studentBId;
    private String routineBId;
    private String studentAId;
    private String routineAId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        tokenA = "Bearer " + jwtService.generateToken("trainerA@test.com");
        tokenB = "Bearer " + jwtService.generateToken("trainerB@test.com");

        studentBId = "cccccccc-cccc-cccc-cccc-cccccccccccc";
        studentAId = "dddddddd-dddd-dddd-dddd-dddddddddddd";
        routineBId = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";
        routineAId = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    }

    private String extractId(String json) throws Exception {
        return objectMapper.readTree(json).get("id").asText();
    }

    private AssignRoutineRequest buildAssignRequest(String routineTemplateId, String studentId) {
        var request = new AssignRoutineRequest();
        request.setRoutineTemplateId(routineTemplateId);
        request.setStudentId(studentId);
        request.setCoachName("Coach Test");
        return request;
    }

    @Test
    void assign_ShouldRejectWhenStudentBelongsToAnotherTrainer() throws Exception {
        var request = buildAssignRequest(routineAId, studentBId);
        mockMvc.perform(post("/assigned-routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assign_ShouldRejectWhenTemplateBelongsToAnotherTrainer() throws Exception {
        var request = buildAssignRequest(routineBId, studentAId);
        mockMvc.perform(post("/assigned-routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_ShouldDenyAccessToOtherTrainerRoutine() throws Exception {
        var assigned = createAssignedRoutineForB();
        mockMvc.perform(get("/assigned-routines/{id}", assigned.getId()).header("Authorization", tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_ShouldDenyAccessToOtherTrainerRoutine() throws Exception {
        var assigned = createAssignedRoutineForB();
        mockMvc.perform(delete("/assigned-routines/{id}", assigned.getId()).header("Authorization", tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assign_ShouldCreateAndPersistBlockDataAsJsonb() throws Exception {
        var request = buildAssignRequest(routineAId, studentAId);
        var response = mockMvc.perform(post("/assigned-routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var assignedId = extractId(response);

        entityManager.flush();
        entityManager.clear();

        var assigned = assignedRoutineRepository.findById(assignedId).orElseThrow();
        assertEquals("Template A", assigned.getName());
        assertEquals(1, assigned.getDays().size());

        var day = assigned.getDays().get(0);
        assertEquals(1, day.getBlocks().size());

        var block = day.getBlocks().get(0);
        assertNotNull(block.getBlockData());
        assertTrue(block.getBlockData().contains("\"series\""), "blockData should contain series field");
        assertTrue(block.getBlockData().contains("Press Banca"), "blockData should contain exercise name");
    }

    @Test
    void update_ShouldModifyOnlyStudentGoalWithoutDestroyingStructure() throws Exception {
        var request = buildAssignRequest(routineAId, studentAId);
        var createResponse = mockMvc.perform(post("/assigned-routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var assignedId = extractId(createResponse);

        var updateRequest = new AssignRoutineRequest();
        updateRequest.setRoutineTemplateId(routineAId);
        updateRequest.setStudentId(studentAId);
        updateRequest.setCoachName("Coach Updated");

        mockMvc.perform(put("/assigned-routines/{id}", assignedId).header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coachName").value("Coach Updated"))
                .andExpect(jsonPath("$.days.length()").value(1))
                .andExpect(jsonPath("$.days[0].blocks.length()").value(1));

        entityManager.flush();
        entityManager.clear();

        var assigned = assignedRoutineRepository.findById(assignedId).orElseThrow();
        assertEquals("Coach Updated", assigned.getCoachName());
        assertEquals(1, assigned.getDays().size());
        assertEquals(1, assigned.getDays().get(0).getBlocks().size());
    }

    @Test
    void delete_ShouldCascadeDeleteDaysAndBlocks() throws Exception {
        var request = buildAssignRequest(routineAId, studentAId);
        var createResponse = mockMvc.perform(post("/assigned-routines").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var assignedId = extractId(createResponse);

        mockMvc.perform(delete("/assigned-routines/{id}", assignedId).header("Authorization", tokenA))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertFalse(assignedRoutineRepository.findById(assignedId).isPresent());
    }

    @Test
    void getByShareUrl_ShouldReturnRoutineWithoutAuth() throws Exception {
        var assigned = createAssignedRoutineForB();
        var shareUrl = assigned.getShareUrl();

        mockMvc.perform(get("/assigned-routines/shared/{shareUrl}", shareUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Template B"))
                .andExpect(jsonPath("$.shareUrl").value(shareUrl));
    }

    @Test
    void list_ShouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/assigned-routines"))
                .andExpect(status().isUnauthorized());
    }

    private AssignedRoutine createAssignedRoutineForB() {
        var trainerB = trainerRepository.findByEmail("trainerB@test.com").orElseThrow();
        var student = studentRepository.findById(studentBId).orElseThrow();
        var template = routineRepository.findById(routineBId).orElseThrow();

        var assigned = new AssignedRoutine();
        assigned.setTrainer(trainerB);
        assigned.setStudent(student);
        assigned.setName(template.getName());
        assigned.setDescription(template.getDescription());
        assigned.setStudentName(student.getFullName());
        assigned.setStudentGoal(student.getObjetivos());
        assigned.setCoachName("Coach B");
        assigned.setShareUrl(java.util.UUID.randomUUID().toString());

        for (var day : template.getDays()) {
            var assignedDay = new AssignedRoutineDay();
            assignedDay.setAssignedRoutine(assigned);
            assignedDay.setDayId(day.getId());
            assignedDay.setName(day.getName());
            assignedDay.setOrderIndex(day.getOrderIndex());
            assigned.getDays().add(assignedDay);

            for (var block : day.getBlocks()) {
                var assignedBlock = new AssignedBlock();
                assignedBlock.setAssignedDay(assignedDay);
                assignedBlock.setBlockData(serializeBlockForTest(block));
                assignedDay.getBlocks().add(assignedBlock);
            }
        }

        return assignedRoutineRepository.save(assigned);
    }

    private String serializeBlockForTest(ExerciseBlock block) {
        try {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("id", block.getId());
            map.put("_combined", block.getIsCombined());
            map.put("series", block.getSeries());
            map.put("reps", block.getReps());
            map.put("rest_time", block.getRestTime());
            map.put("indications", block.getIndications());
            var subs = block.getSubExercises().stream().map(sub -> {
                var subMap = new java.util.LinkedHashMap<String, Object>();
                subMap.put("exercise_id", sub.getExerciseId());
                subMap.put("name", sub.getName());
                subMap.put("gif_url", sub.getGifUrl());
                subMap.put("video_url", sub.getVideoUrl());
                subMap.put("instructions", sub.getInstructions());
                return subMap;
            }).toList();
            map.put("sub_exercises", subs);
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
