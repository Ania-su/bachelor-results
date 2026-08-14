package school.hei.demo.IT;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.demo.conf.FacadeIT;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.entity.JUser;

class UserRepositoryIT extends FacadeIT {
  @Autowired UserRepository userRepository;
  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanUsers() {
    userRepository.deleteAll();
  }

  @Test
  void shouldPersistUserWithDatabaseColumnMapping() {
    UUID specialtyId =
        jdbcTemplate.queryForObject("select id from specialty where code = 'NONE'", UUID.class);
    JUser user = new JUser();
    user.setReference("REF001");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john@example.com");
    user.setPassword("hashed-password");
    user.setUserRole(UserRole.STUDENT);
    user.setSpecialtyId(specialtyId);

    JUser saved = userRepository.saveAndFlush(user);

    assertTrue(userRepository.existsByReference(saved.getReference()));
    assertTrue(userRepository.existsByEmailIgnoreCase("JOHN@EXAMPLE.COM"));
  }

  @Test
  void shouldFilterUsersCaseInsensitivelyWithPartialMatches() {
    UUID specialtyId =
        jdbcTemplate.queryForObject("select id from specialty where code = 'NONE'", UUID.class);
    userRepository.saveAllAndFlush(
        List.of(
            user("REF001", "John", "Doe", "john@example.com", UserRole.STUDENT, specialtyId),
            user("REF002", "Joanna", "Smith", "joanna@school.org", UserRole.TEACHER, specialtyId),
            user("REF003", "Alice", "Doe", "alice@example.com", UserRole.STUDENT, specialtyId)));

    Page<JUser> result =
        userRepository.findAllByFilters(
            UserRole.STUDENT, "ref00", "jo", "do", "example", PageRequest.of(0, 20));

    assertEquals(1, result.getTotalElements());
    assertEquals("REF001", result.getContent().get(0).getReference());
  }

  @Test
  void shouldApplyPagination() {
    UUID specialtyId =
        jdbcTemplate.queryForObject("select id from specialty where code = 'NONE'", UUID.class);
    userRepository.saveAllAndFlush(
        List.of(
            user("REF001", "John", "Doe", "one@example.com", UserRole.STUDENT, specialtyId),
            user("REF002", "Jane", "Doe", "two@example.com", UserRole.STUDENT, specialtyId),
            user("REF003", "Jack", "Doe", "three@example.com", UserRole.STUDENT, specialtyId)));

    Page<JUser> result =
        userRepository.findAllByFilters(null, null, null, null, null, PageRequest.of(1, 2));

    assertEquals(3, result.getTotalElements());
    assertEquals(1, result.getNumber());
    assertEquals(1, result.getNumberOfElements());
  }

  private JUser user(
      String reference,
      String firstName,
      String lastName,
      String email,
      UserRole role,
      UUID specialtyId) {
    JUser user = new JUser();
    user.setReference(reference);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPassword("hashed-password");
    user.setUserRole(role);
    user.setSpecialtyId(specialtyId);
    return user;
  }
}
