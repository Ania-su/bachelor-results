package school.hei.demo.IT;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
}
