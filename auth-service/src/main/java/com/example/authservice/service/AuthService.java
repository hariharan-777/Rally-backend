package com.example.authservice.service;

import com.example.authservice.dto.AuthDtos;
import com.example.authservice.jwt.JwtService;
import com.example.authservice.model.AuthUser;
import com.example.authservice.repository.AuthUserRepository;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AuthUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthUserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }

        AuthUser user = new AuthUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        try {
            repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            // The database unique constraint is authoritative under concurrent registrations.
            throw new EmailAlreadyUsedException();
        }
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request) {
        AuthUser user = repository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return tokenFor(user);
    }

    private AuthDtos.TokenResponse tokenFor(AuthUser user) {
        return new AuthDtos.TokenResponse(
                jwtService.generateToken(user.getEmail(), user.getRole()), user.getEmail(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static final class EmailAlreadyUsedException extends RuntimeException {
    }

    public static final class InvalidCredentialsException extends RuntimeException {
    }
}
