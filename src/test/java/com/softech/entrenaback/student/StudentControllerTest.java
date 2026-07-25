package com.softech.entrenaback.student;

import com.softech.entrenaback.auth.JwtService;
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
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Sql(scripts = "/student-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class StudentControllerTest {

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
    private StudentRepository studentRepository;

    private MockMvc mockMvc;
    private String tokenA;
    private String tokenB;
    private String studentBId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        tokenA = "Bearer " + jwtService.generateToken("trainerA@test.com");
        tokenB = "Bearer " + jwtService.generateToken("trainerB@test.com");
        studentBId = "cccccccc-cccc-cccc-cccc-cccccccccccc";
    }

    @Test
    void list_ShouldReturnEmpty_WhenTrainerAHasNoStudents() throws Exception {
        mockMvc.perform(get("/students").header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getById_ShouldReject_WhenTrainerATriesToReadStudentOfB() throws Exception {
        mockMvc.perform(get("/students/{id}", studentBId)
                .header("Authorization", tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_ShouldReject_WhenTrainerATriesToDeleteStudentOfB() throws Exception {
        mockMvc.perform(delete("/students/{id}", studentBId)
                .header("Authorization", tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturn201AndPersistStudent() throws Exception {
        var body = """
            {
                "fullName": "New Student",
                "age": 22,
                "gender": "male",
                "disciplina": "Musculacion",
                "objetivos": "Hipertrofia"
            }
            """;

        var result = mockMvc.perform(post("/students")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.fullName").value("New Student"))
                .andExpect(jsonPath("$.age").value(22))
                .andReturn();

        var responseJson = result.getResponse().getContentAsString();
        var idStr = responseJson.replaceFirst(".*\"id\":\"([^\"]+)\".*", "$1");
        var found = studentRepository.findById(idStr).orElseThrow();
        assert found.getFullName().equals("New Student") : "Student name should be correct";
        assert found.getTrainer().getId().equals("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") : "Student should belong to trainer A";
    }

    @Test
    void update_ShouldReturn200AndUpdatedData_WhenValid() throws Exception {
        var createBody = """
            { "fullName": "Original", "age": 20 }
            """;
        var createResult = mockMvc.perform(post("/students")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        var createdId = createResult.getResponse().getContentAsString()
                .replaceFirst(".*\"id\":\"([^\"]+)\".*", "$1");

        var updateBody = """
            { "fullName": "Updated Name", "age": 25, "objetivos": "Nuevo objetivo" }
            """;

        var updateResult = mockMvc.perform(put("/students/{id}", createdId)
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isOk())
                .andReturn();

        var updatedJson = updateResult.getResponse().getContentAsString();
        assert updatedJson.contains("\"fullName\":\"Updated Name\"") : "Full name should be updated";
        assert updatedJson.contains("\"age\":25") : "Age should be updated";
        assert updatedJson.contains("\"objetivos\":\"Nuevo objetivo\"") : "Objetivos should be updated";
    }

    @Test
    void delete_ShouldReturn204AndRemoveFromDatabase() throws Exception {
        var createBody = """
            { "fullName": "To Delete", "age": 30 }
            """;
        var createResult = mockMvc.perform(post("/students")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        var createdId = createResult.getResponse().getContentAsString()
                .replaceFirst(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(delete("/students/{id}", createdId)
                .header("Authorization", tokenA))
                .andExpect(status().isNoContent());

        var exists = studentRepository.findById(createdId);
        assert exists.isEmpty() : "Student should be removed from DB";
    }

    @Test
    void anyEndpoint_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/students")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/students").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/students/some-id").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/students/some-id")).andExpect(status().isUnauthorized());
    }
}
