package com.softech.entrenaback.exercise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class ExerciseRepositoryTest {

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
    private ExerciseRepository exerciseRepository;

    @Test
    void search_ShouldReturnResults_WhenAllFiltersNull() {
        Page<Exercise> result = exerciseRepository.search(null, null, null, PageRequest.of(0, 10));

        assertThat(result).isNotEmpty();
    }

    @Test
    void search_ShouldFilterByMuscle_WhenMuscleProvided() {
        Page<Exercise> result = exerciseRepository.search(null, "abdominales", null, PageRequest.of(0, 100));

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getTargetMuscles().toLowerCase().contains("abdominales"));
    }

    @Test
    void getById_ShouldReturnInstructionsAsList() {
        Page<Exercise> page = exerciseRepository.search(null, null, null, PageRequest.of(0, 1));
        Exercise first = page.getContent().get(0);

        Exercise found = exerciseRepository.findById(first.getId()).orElseThrow();

        assertThat(found.getInstructions()).isNotEmpty();
    }

    @Test
    void findDistinctTargetMuscles_ShouldReturnSortedNoDuplicates() {
        List<String> muscles = exerciseRepository.findDistinctTargetMuscles();

        assertThat(muscles).isNotEmpty();
        assertThat(muscles).doesNotHaveDuplicates();
        assertThat(muscles).isSortedAccordingTo(String::compareToIgnoreCase);
    }
}
