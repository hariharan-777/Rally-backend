package com.example.authservice.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "test-secret-with-at-least-thirty-two-bytes";

    @Test
    void generatedTokenCanBeParsed() {
        JwtService service = new JwtService(SECRET, 60);

        var claims = service.parse(service.generateToken("member@example.com", "USER"));

        assertEquals("member@example.com", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void rejectsTokensSignedWithAnotherKey() {
        JwtService issuer = new JwtService(SECRET, 60);
        JwtService validator = new JwtService("another-secret-with-at-least-thirty-two-bytes", 60);

        assertThrows(JwtException.class, () -> validator.parse(issuer.generateToken("member@example.com", "USER")));
    }
}
