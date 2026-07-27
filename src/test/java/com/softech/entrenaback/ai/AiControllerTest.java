package com.softech.entrenaback.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softech.entrenaback.ai.dto.AiGenerateRequest;
import com.softech.entrenaback.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Sql(scripts = "/student-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AiControllerTest {

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
    private RestTemplate restTemplate;

    private MockMvc mockMvc;
    private MockRestServiceServer mockServer;
    private String tokenA;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        tokenA = "Bearer " + jwtService.generateToken("trainerA@test.com");
    }

    private AiGenerateRequest buildRequest(String exerciseName) {
        return new AiGenerateRequest(exerciseName);
    }

    private String geminiSuccessResponse() {
        return """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "{\\"name\\": \\"Press Banca\\", \\"muscle\\": \\"Pecho\\", \\"equipment\\": \\"Barra\\", \\"target\\": \\"Pectorales\\", \\"secondaryMuscles\\": [\\"Tríceps\\"], \\"instructions\\": [\\"Calentar\\", \\"Ejecutar\\"]}"
                      }
                    ]
                  }
                }
              ]
            }
            """;
    }

    @Test
    void generate_ShouldReturnStructuredExerciseFromGemini() throws Exception {
        var request = buildRequest("Press Banca");

        mockServer.expect(once(),
                requestTo("http://localhost:8089/test-endpoint?key=api-key-test"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(geminiSuccessResponse(), MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/ai/generate-exercise").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercise.name").value("Press Banca"))
                .andExpect(jsonPath("$.exercise.muscle").value("Pecho"))
                .andExpect(jsonPath("$.exercise.equipment").value("Barra"))
                .andExpect(jsonPath("$.exercise.target").value("Pectorales"))
                .andExpect(jsonPath("$.exercise.custom").value(true));

        mockServer.verify();
    }

    @Test
    void generate_ShouldIncludeSystemPromptInRequestBody() throws Exception {
        var request = buildRequest("Remo");

        mockServer.expect(once(),
                requestTo("http://localhost:8089/test-endpoint?key=api-key-test"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Responde SOLO con JSON")))
                .andRespond(withSuccess(geminiSuccessResponse(), MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/ai/generate-exercise").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void generate_ShouldHandleServerErrorFromGemini() throws Exception {
        var request = buildRequest("Sentadilla");

        mockServer.expect(once(),
                requestTo("http://localhost:8089/test-endpoint?key=api-key-test"))
                .andRespond(withServerError().body("Internal Server Error"));

        mockMvc.perform(post("/ai/generate-exercise").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        mockServer.verify();
    }

    @Test
    void generate_ShouldHandleRateLimitFromGemini() throws Exception {
        var request = buildRequest("Peso muerto");

        mockServer.expect(once(),
                requestTo("http://localhost:8089/test-endpoint?key=api-key-test"))
                .andRespond(withTooManyRequests());

        mockMvc.perform(post("/ai/generate-exercise").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        mockServer.verify();
    }

    @Test
    void generate_ShouldHandleInvalidJsonFromGemini() throws Exception {
        var request = buildRequest("Curl");

        var invalidResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "Este es un texto libre que no es JSON válido"
                      }
                    ]
                  }
                }
              ]
            }
            """;

        mockServer.expect(once(),
                requestTo("http://localhost:8089/test-endpoint?key=api-key-test"))
                .andRespond(withSuccess(invalidResponse, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/ai/generate-exercise").header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        mockServer.verify();
    }

    @Test
    void generate_ShouldRejectRequestWithoutToken() throws Exception {
        var request = buildRequest("Press Banca");

        mockMvc.perform(post("/ai/generate-exercise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
