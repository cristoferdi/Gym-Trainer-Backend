package com.softech.entrenaback.auth;

import com.softech.entrenaback.trainer.Trainer;
import java.time.LocalDateTime;

public record UserProfile(
    String id,
    String email,
    String fullName,
    String phone,
    LocalDateTime createdAt
) {
    static UserProfile from(Trainer trainer) {
        return new UserProfile(
            trainer.getId(),
            trainer.getEmail(),
            trainer.getFullName(),
            trainer.getPhone(),
            trainer.getCreatedAt()
        );
    }
}
