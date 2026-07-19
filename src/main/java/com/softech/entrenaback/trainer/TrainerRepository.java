package com.softech.entrenaback.trainer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, String> {
    Optional<Trainer> findByEmail(String email);
    boolean existsByEmail(String email);
}
