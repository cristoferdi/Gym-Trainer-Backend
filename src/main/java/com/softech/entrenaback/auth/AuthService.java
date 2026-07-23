package com.softech.entrenaback.auth;

import com.softech.entrenaback.config.DuplicateResourceException;
import com.softech.entrenaback.trainer.Trainer;
import com.softech.entrenaback.trainer.TrainerRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(TrainerRepository trainerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.trainerRepository = trainerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (trainerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("El email ya está registrado");
        }

        var trainer = new Trainer(
            request.fullName(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.phone()
        );
        trainer = trainerRepository.save(trainer);

        var token = jwtService.generateToken(trainer.getEmail());
        return new AuthResponse(token, UserProfile.from(trainer));
    }

    public AuthResponse login(LoginRequest request) {
        var trainer = trainerRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), trainer.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        var token = jwtService.generateToken(trainer.getEmail());
        return new AuthResponse(token, UserProfile.from(trainer));
    }

    public AuthResponse refresh(String email) {
        var trainer = trainerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        var token = jwtService.generateToken(email);
        return new AuthResponse(token, UserProfile.from(trainer));
    }

    public UserProfile getProfile(String email) {
        var trainer = trainerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return UserProfile.from(trainer);
    }
}
