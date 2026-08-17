package school.hei.demo.conf;

import java.util.UUID;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.demo.domain.dto.request.LoginRequest;
import school.hei.demo.domain.dto.response.AuthResponse;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JSpecialty;
import school.hei.demo.repository.model.JUser;

public final class TestAuthentication {
  private static final String EMAIL = "integration-test@example.com";
  private static final String PASSWORD = "integration-password";

  private TestAuthentication() {}

  public static void configure(
      TestRestTemplate restTemplate,
      UserRepository userRepository,
      SpecialtyRepository specialtyRepository,
      PasswordEncoder passwordEncoder) {
    JUser user =
        userRepository
            .findByEmailIgnoreCase(EMAIL)
            .orElseGet(() -> createUser(userRepository, specialtyRepository, passwordEncoder));

    HttpHeaders loginHeaders = new HttpHeaders();
    loginHeaders.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<AuthResponse> loginResponse =
        restTemplate.postForEntity(
            "/auth/login",
            new HttpEntity<>(new LoginRequest(user.getEmail(), PASSWORD), loginHeaders),
            AuthResponse.class);

    String token = loginResponse.getBody().getAccessToken();
    restTemplate.getRestTemplate().getInterceptors().clear();
    restTemplate
        .getRestTemplate()
        .getInterceptors()
        .add(
            (request, body, execution) -> {
              request.getHeaders().setBearerAuth(token);
              return execution.execute(request, body);
            });
  }

  private static JUser createUser(
      UserRepository userRepository,
      SpecialtyRepository specialtyRepository,
      PasswordEncoder passwordEncoder) {
    JSpecialty specialty = specialtyRepository.findAll().get(0);
    JUser user = new JUser();
    user.setId(UUID.randomUUID());
    user.setReference("ITAUTH");
    user.setFirstName("Integration");
    user.setLastName("Test");
    user.setEmail(EMAIL);
    user.setPassword(passwordEncoder.encode(PASSWORD));
    user.setUserRole(UserRole.ADMIN);
    user.setSpecialtyId(specialty.getId());
    return userRepository.save(user);
  }
}
