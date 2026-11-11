package elp.edu.pe.horario.shared.utils;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {
    private static JwtUtils jwt(long expiration) {
        var jwt = new JwtUtils();
        ReflectionTestUtils.setField(jwt, "SECRE_KEY", "synthetic-test-secret-at-least-32-bytes-long");
        ReflectionTestUtils.setField(jwt, "EXPIRATION_TIME", expiration);
        return jwt;
    }

    @Test void roundTripPreservesSubjectUsernameAndRole() {
        var jwt = jwt(60_000);
        var token = jwt.generateToken("coordinador", "COORDINADOR");
        assertEquals("coordinador", jwt.extracUsername(token));
        assertEquals("coordinador", jwt.extractAllClaims(token).get("username"));
        assertEquals("COORDINADOR", jwt.extractRole(token));
        assertTrue(jwt.isTokenValid(token, "coordinador"));
        assertFalse(jwt.isTokenValid(token, "otro"));
    }

    @Test void signatureRemainsHs256() {
        var token = jwt(60_000).generateToken("user", "ADMIN");
        var header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);
        assertTrue(header.contains("\"alg\":\"HS256\""));
    }

    @Test void rejectsExpiredTokens() {
        var jwt = jwt(-60_000);
        var token = jwt.generateToken("user", "ADMIN");
        assertThrows(JwtException.class, () -> jwt.extractAllClaims(token));
    }

    @Test void rejectsTamperedPayload() {
        var jwt = jwt(60_000);
        var parts = jwt.generateToken("user", "COORDINADOR").split("\\.");
        parts[1] = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"attacker\",\"role\":\"ADMIN\"}".getBytes(StandardCharsets.UTF_8));
        assertThrows(JwtException.class, () -> jwt.extractAllClaims(String.join(".", parts)));
    }
}
