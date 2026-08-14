package school.hei.demo.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.demo.domain.entity.User;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.entity.JUser;

class UserMapperTest {
  private final UserMapper mapper = new UserMapper();

  @Test
  void shouldMapJpaUserToDomainUser() {
    JUser source = jpaUser();

    User result = mapper.toDomain(source);

    assertEquals(source.getId(), result.getId());
    assertEquals(source.getReference(), result.getReference());
    assertEquals(source.getSpecialtyId(), result.getSpecialtyId());
    assertEquals(source.getPassword(), result.getPassword());
    assertEquals(source.getCreatedAt(), result.getCreatedAt());
  }

  @Test
  void shouldMapDomainUserToJpaUser() {
    User source = domainUser();

    JUser result = mapper.toJpa(source);

    assertEquals(source.getId(), result.getId());
    assertEquals(source.getEmail(), result.getEmail());
    assertEquals(source.getSpecialtyId(), result.getSpecialtyId());
    assertEquals(source.getUserRole(), result.getUserRole());
  }

  @Test
  void shouldReturnNullForNullInputs() {
    assertNull(mapper.toDomain(null));
    assertNull(mapper.toJpa(null));
  }

  private JUser jpaUser() {
    JUser user = new JUser();
    user.setId(UUID.randomUUID());
    user.setReference("REF001");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john@example.com");
    user.setPassword("hashed");
    user.setUserRole(UserRole.STUDENT);
    user.setSpecialtyId(UUID.randomUUID());
    user.setEntryYear(2025);
    user.setCreatedAt(Instant.now());
    return user;
  }

  private User domainUser() {
    return new User(
        UUID.randomUUID(),
        "REF001",
        "John",
        "Doe",
        "john@example.com",
        "hashed",
        UserRole.STUDENT,
        UUID.randomUUID(),
        2025,
        Instant.now());
  }
}
