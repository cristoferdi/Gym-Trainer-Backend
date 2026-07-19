package com.softech.entrenaback.auth;

public record AuthResponse(
    String token,
    UserProfile user
) {}
