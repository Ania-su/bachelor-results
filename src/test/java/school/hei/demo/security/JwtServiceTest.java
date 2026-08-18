package school.hei.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.JwtException;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
  private JwtService jwtService;

  @BeforeEach
  void setUp() throws Exception {
    jwtService = new JwtService();
    Field secret = JwtService.class.getDeclaredField("jwtSecret");
    secret.setAccessible(true);
    secret.set(jwtService, "this-is-a-test-secret-key-at-least-32-chars-long");
  }

  @Test
  void generateToken_producesParsableClaims() {
    UUID userId = UUID.randomUUID();
    String token = jwtService.generateToken(userId, "user@hei.mg", UserRole.ADMIN);

    assertNotNull(token);
    assertEquals(userId.toString(), jwtService.extractSubject(token));
    assertEquals("user@hei.mg", jwtService.extractEmail(token));
    assertEquals(UserRole.ADMIN.name(), jwtService.extractRole(token));
    assertTrue(jwtService.isTokenValid(token));
  }

  @Test
  void extractClaim_returnsTypedClaim() {
    String token = jwtService.generateToken(UUID.randomUUID(), "user@hei.mg", UserRole.STUDENT);
    assertEquals("user@hei.mg", jwtService.extractClaim(token, "email", String.class));
  }

  @Test
  void getTokenLifetimeSeconds_isOneHour() {
    assertEquals(3600, jwtService.getTokenLifetimeSeconds());
  }

  @Test
  void isTokenValid_returnsFalseForTamperedToken() {
    String token = jwtService.generateToken(UUID.randomUUID(), "user@hei.mg", UserRole.ADMIN);
    String tampered = token.substring(0, token.length() - 2) + "xx";
    assertFalse(jwtService.isTokenValid(tampered));
  }

  @Test
  void extractSubject_throwsForInvalidToken() {
    assertThrows(JwtException.class, () -> jwtService.extractSubject("not-a-token"));
    assertThrows(JwtException.class, () -> jwtService.extractEmail("not-a-token"));
  }
}
