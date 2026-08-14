package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.request.UserUpdate;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.UserPage;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.ConflictException;
import school.hei.demo.exception.NotFoundException;
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
    request =
        new UserCreate(
            "REF001",
            "John",
            "Doe",
            "john@example.com",
            "password",
            UserRole.STUDENT,
            UUID.randomUUID(),
            2025);
  }

  @Test
  void shouldCreateUserAndHashPassword() {
    JUser savedJpaUser = new JUser();
    school.hei.demo.domain.entity.User savedDomainUser =
        new school.hei.demo.domain.entity.User(
            UUID.randomUUID(),
            request.reference(),
            request.firstName(),
            request.lastName(),
            request.email(),
            "hashed",
            request.userRole(),
            request.specialtyId(),
            request.entryYear(),
            null);
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

  @Test
  void shouldFindUsersWithPaginationAndFilters() {
    JUser jUser = new JUser();
    school.hei.demo.domain.entity.User domainUser =
        new school.hei.demo.domain.entity.User(
            UUID.randomUUID(),
            "REF001",
            "John",
            "Doe",
            "john@example.com",
            "hashed",
            UserRole.STUDENT,
            request.specialtyId(),
            2025,
            null);
    when(userRepository.findAllByFilters(
            eq(UserRole.STUDENT), eq("ref"), eq("john"), eq("doe"), eq("example"), any()))
        .thenReturn(new PageImpl<>(List.of(jUser), PageRequest.of(1, 2), 3));
    when(userMapper.toDomain(jUser)).thenReturn(domainUser);

    UserPage result =
        userService.findAll(1, 2, UserRole.STUDENT, " ref ", "john", "doe", "example");

    assertEquals(1, result.getContent().size());
    assertEquals(1, result.getPage().getPage());
    assertEquals(2, result.getPage().getPageSize());
    assertEquals(3, result.getPage().getTotalElements());
    assertEquals(2, result.getPage().getTotalPages());
    verify(userValidator).validatePagination(1, 2);
  }

  @Test
  void shouldNormalizeBlankFiltersToNull() {
    when(userRepository.findAllByFilters(eq(null), eq(null), eq(null), eq(null), eq(null), any()))
        .thenReturn(new PageImpl<>(List.of()));

    userService.findAll(0, 20, null, " ", null, "", " ");

    verify(userRepository)
        .findAllByFilters(eq(null), eq(null), eq(null), eq(null), eq(null), any());
  }

  @Test
  void shouldRejectInvalidPagination() {
    doThrow(new BadRequestException("invalid page")).when(userValidator).validatePagination(-1, 20);

    assertThrows(
        BadRequestException.class, () -> userService.findAll(-1, 20, null, null, null, null, null));
  }

  @Test
  void shouldFindUserByStringUuid() {
    UUID id = UUID.randomUUID();
    JUser jUser = existingUser(id);
    school.hei.demo.domain.entity.User domainUser = domainUser(id);
    when(userValidator.validateUuid(id.toString())).thenReturn(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(jUser));
    when(userMapper.toDomain(jUser)).thenReturn(domainUser);

    User result = userService.findById(id.toString());

    assertEquals(id, result.getId());
    assertEquals(jUser.getEmail(), result.getEmail());
  }

  @Test
  void shouldRejectUnknownUserWhenFindingById() {
    UUID id = UUID.randomUUID();
    when(userValidator.validateUuid(id.toString())).thenReturn(id);
    when(userRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.findById(id.toString()));
  }

  @Test
  void shouldUpdateOnlyProvidedFieldsAndHashPassword() {
    UUID id = UUID.randomUUID();
    JUser jUser = existingUser(id);
    UserUpdate request =
        new UserUpdate(null, "Updated", null, null, "new-password", null, null, null);
    when(userValidator.validateUuid(id.toString())).thenReturn(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(jUser));
    when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
    when(userRepository.save(jUser)).thenReturn(jUser);
    when(userMapper.toDomain(jUser)).thenReturn(domainUser(id));

    User result = userService.update(id.toString(), request);

    assertEquals("Updated", jUser.getFirstName());
    assertEquals("Doe", jUser.getLastName());
    assertEquals("new-hash", jUser.getPassword());
    assertEquals(id, result.getId());
  }

  @Test
  void shouldRejectDuplicateValuesDuringUpdate() {
    UUID id = UUID.randomUUID();
    JUser jUser = existingUser(id);
    when(userValidator.validateUuid(id.toString())).thenReturn(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(jUser));
    when(userRepository.existsByReferenceAndIdNot("OTHER", id)).thenReturn(true);

    assertThrows(
        ConflictException.class,
        () ->
            userService.update(
                id.toString(), new UserUpdate("OTHER", null, null, null, null, null, null, null)));
  }

  @Test
  void shouldAllowEmptyUpdateAndDeleteExistingUser() {
    UUID id = UUID.randomUUID();
    JUser jUser = existingUser(id);
    when(userValidator.validateUuid(id.toString())).thenReturn(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(jUser));
    when(userRepository.save(jUser)).thenReturn(jUser);
    when(userMapper.toDomain(jUser)).thenReturn(domainUser(id));

    userService.update(
        id.toString(), new UserUpdate(null, null, null, null, null, null, null, null));
    userService.delete(id.toString());

    verify(userRepository).save(jUser);
    verify(userRepository).delete(jUser);
  }

  @Test
  void shouldRejectUnknownUserWhenDeleting() {
    UUID id = UUID.randomUUID();
    when(userValidator.validateUuid(id.toString())).thenReturn(id);
    when(userRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.delete(id.toString()));
  }

  private JUser existingUser(UUID id) {
    JUser user = new JUser();
    user.setId(id);
    user.setReference("REF001");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john@example.com");
    user.setPassword("hash");
    user.setUserRole(UserRole.STUDENT);
    user.setSpecialtyId(request.specialtyId());
    user.setEntryYear(2025);
    return user;
  }

  private school.hei.demo.domain.entity.User domainUser(UUID id) {
    return new school.hei.demo.domain.entity.User(
        id,
        "REF001",
        "John",
        "Doe",
        "john@example.com",
        "hash",
        UserRole.STUDENT,
        request.specialtyId(),
        2025,
        null);
  }
}
