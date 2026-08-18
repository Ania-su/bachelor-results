package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.demo.domain.dto.request.LoginRequest;
import school.hei.demo.domain.dto.response.AuthResponse;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.entity.User;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.UnauthorizedException;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JUser;
import school.hei.demo.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock UserRepository userRepository;
  @Mock UserMapper userMapper;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtService jwtService;
  @InjectMocks AuthService service;

  private JUser jUser(String email, String password) {
    JUser u = new JUser();
    u.setId(UUID.randomUUID());
    u.setReference("REF");
    u.setFirstName("A");
    u.setLastName("B");
    u.setEmail(email);
    u.setPassword(password);
    u.setUserRole(UserRole.ADMIN);
    u.setSpecialtyId(UUID.randomUUID());
    return u;
  }

  @Test
  void login_unknownEmail_throws() {
    when(userRepository.findByEmailIgnoreCase("missing@hei.mg"))
        .thenReturn(java.util.Optional.empty());
    assertThrows(
        UnauthorizedException.class, () -> service.login(new LoginRequest("missing@hei.mg", "pw")));
  }

  @Test
  void login_wrongPassword_throws() {
    JUser u = jUser("user@hei.mg", "hash");
    when(userRepository.findByEmailIgnoreCase("user@hei.mg")).thenReturn(java.util.Optional.of(u));
    when(passwordEncoder.matches("bad", "hash")).thenReturn(false);
    assertThrows(
        UnauthorizedException.class, () -> service.login(new LoginRequest("user@hei.mg", "bad")));
  }

  @Test
  void login_success_returnsTokenAndUser() {
    JUser u = jUser("user@hei.mg", "hash");
    User domain = new User();
    domain.setId(u.getId());
    domain.setReference("REF");
    domain.setUserRole(UserRole.ADMIN);
    domain.setSpecialtyId(u.getSpecialtyId());
    when(userRepository.findByEmailIgnoreCase("user@hei.mg")).thenReturn(java.util.Optional.of(u));
    when(passwordEncoder.matches("pw", "hash")).thenReturn(true);
    when(userMapper.toDomain(u)).thenReturn(domain);
    when(jwtService.generateToken(u.getId(), u.getEmail(), u.getUserRole()))
        .thenReturn("token-abc");
    when(jwtService.getTokenLifetimeSeconds()).thenReturn(3600);

    AuthResponse response = service.login(new LoginRequest("user@hei.mg", "pw"));

    assertEquals("token-abc", response.getAccessToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(3600, response.getExpiresIn());
    assertEquals("REF", response.getUser().getReference());
  }
}
