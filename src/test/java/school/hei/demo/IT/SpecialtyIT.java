package school.hei.demo.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.http.HttpClient;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.demo.conf.FacadeIT;
import school.hei.demo.conf.TestAuthentication;
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.enums.CodeType;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

class SpecialtyIT extends FacadeIT {
  @Autowired TestRestTemplate restTemplate;
  @Autowired JdbcTemplate jdbcTemplate;
  @Autowired UserRepository userRepository;
  @Autowired SpecialtyRepository specialtyRepository;
  @Autowired PasswordEncoder passwordEncoder;

  @BeforeEach
  void resetSeedSpecialties() {
    jdbcTemplate.execute("DELETE FROM users");
    jdbcTemplate.execute("DELETE FROM course_specialty");
    jdbcTemplate.execute("DELETE FROM specialty");
    jdbcTemplate.update(
        "INSERT INTO specialty (code, label) VALUES ('NONE', 'Non défini'), ('EL', 'Écosystème"
            + " Logiciel'), ('TN', 'Transformation Numérique')");
    restTemplate
        .getRestTemplate()
        .setRequestFactory(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
    TestAuthentication.configure(restTemplate, userRepository, specialtyRepository, passwordEncoder);
  }

  @AfterEach
  void restoreSeedSpecialties() {
    jdbcTemplate.update(
        "INSERT INTO specialty (code, label) VALUES ('NONE', 'Non défini'), ('EL', 'Écosystème"
            + " Logiciel'), ('TN', 'Transformation Numérique') ON CONFLICT (code) DO UPDATE SET"
            + " label = EXCLUDED.label");
  }

  @Test
  void shouldListAndCompleteCrudWithoutConsumingSeedCodes() {
    assertEquals(
        HttpStatus.OK,
        restTemplate.getForEntity("/specialties", SpecialtyResponse[].class).getStatusCode());
    SpecialtyRequest request = new SpecialtyRequest(CodeType.EL, "Temporary specialty");
    jdbcTemplate.update("DELETE FROM specialty WHERE code = 'EL'");
    ResponseEntity<SpecialtyResponse> created =
        restTemplate.postForEntity("/specialties", json(request), SpecialtyResponse.class);
    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    UUID id = created.getBody().getId();

    ResponseEntity<SpecialtyResponse> updated =
        restTemplate.exchange(
            "/specialties/{id}",
            HttpMethod.PATCH,
            json(new SpecialtyRequest(CodeType.EL, "Updated specialty")),
            SpecialtyResponse.class,
            id);
    assertEquals(HttpStatus.OK, updated.getStatusCode());
    assertEquals("Updated specialty", updated.getBody().getLabel());
    assertEquals(
        HttpStatus.NO_CONTENT,
        restTemplate
            .exchange("/specialties/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, id)
            .getStatusCode());
    assertEquals(
        HttpStatus.NOT_FOUND,
        restTemplate.getForEntity("/specialties/{id}", String.class, id).getStatusCode());
  }

  @Test
  void shouldRejectInvalidInputAndUnknownId() {
    assertEquals(
        HttpStatus.BAD_REQUEST,
        restTemplate
            .postForEntity("/specialties", json(new SpecialtyRequest(null, "label")), String.class)
            .getStatusCode());
    assertEquals(
        HttpStatus.BAD_REQUEST,
        restTemplate.getForEntity("/specialties/not-a-uuid", String.class).getStatusCode());
  }

  private <T> HttpEntity<T> json(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }
}
