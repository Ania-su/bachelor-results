package school.hei.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import school.hei.demo.enums.UserRole;

@Service
public class JwtService {
  private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

  @Value("${spring.jwt.secret}")
  private String jwtSecret;

  public String generateToken(UUID userId, String email, UserRole role) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .claim("role", role.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(TOKEN_LIFETIME)))
        .signWith(getSigningKey())
        .compact();
  }

  public int getTokenLifetimeSeconds() {
    return Math.toIntExact(TOKEN_LIFETIME.toSeconds());
  }

  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  public String extractEmail(String token) {
    return extractClaim(token, "email", String.class);
  }

  public String extractRole(String token) {
    return extractClaim(token, "role", String.class);
  }

  public <T> T extractClaim(String token, String claimName, Class<T> claimType) {
    return parseClaims(token).get(claimName, claimType);
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }
}
