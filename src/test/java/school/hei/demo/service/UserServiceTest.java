package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.ConflictException;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.entity.JUser;
import school.hei.demo.validators.UserValidator;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock UserRepository userRepository;
  @Mock UserMapper userMapper;
  @Mock PasswordEncoder passwordEncoder;
  @Mock UserValidator userValidator;
  @InjectMocks UserService userService;

  private UserCreate request;

  @BeforeEach
  void setUp() {
    request = new UserCreate("REF001", "John", "Doe", "john@example.com", "password", UserRole.STUDENT, UUID.randomUUID(), 2025);
  }

  @Test
  void shouldCreateUserAndHashPassword() {
    JUser savedJpaUser = new JUser();
    school.hei.demo.domain.entity.User savedDomainUser =
        new school.hei.demo.domain.entity.User(
            UUID.randomUUID(), request.reference(), request.firstName(), request.lastName(),
            request.email(), "hashed", request.userRole(), request.specialtyId(),
            request.entryYear(), null);
    when(userRepository.save(any())).thenReturn(savedJpaUser);
    when(userMapper.toJpa(any())).thenReturn(new JUser());
    when(passwordEncoder.encode(request.password())).thenReturn("hashed");
    when(userMapper.toDomain(savedJpaUser)).thenReturn(savedDomainUser);

    User result = userService.create(request);

    assertEquals(request.email(), result.getEmail());
    verify(userValidator).validate(request);
    verify(passwordEncoder).encode(request.password());
    verify(userRepository).save(any(JUser.class));
  }

  @Test
  void shouldRejectDuplicateReference() {
    when(userRepository.existsByReference(request.reference())).thenReturn(true);

    assertThrows(ConflictException.class, () -> userService.create(request));
  }

  @Test
  void shouldRejectDuplicateEmail() {
    when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

    assertThrows(ConflictException.class, () -> userService.create(request));
  }
}
