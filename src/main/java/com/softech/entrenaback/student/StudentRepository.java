package com.softech.entrenaback.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String> {
    List<Student> findByTrainerId(String trainerId);
    List<Student> findByTrainerIdAndUpdatedAtAfter(String trainerId, LocalDateTime since);
}
