package school.hei.demo.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.http.HttpClient;
import java.util.UUID;
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
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.request.UserUpdate;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.UserPage;
import school.hei.demo.enums.UserRole;
import school.hei.demo.repository.UserRepository;

class UserIT extends FacadeIT {
  @Autowired TestRestTemplate restTemplate;
  @Autowired JdbcTemplate jdbcTemplate;
  @Autowired UserRepository userRepository;

  @BeforeEach
  void cleanUsers() {
    userRepository.deleteAll();
    restTemplate
        .getRestTemplate()
        .setRequestFactory(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
  }

  @Test
  void shouldCompleteUserCrudThroughHttpFlow() {
    UUID specialtyId = specialtyId();
    UserCreate createRequest =
        new UserCreate(
            "REF001",
            "John",
            "Doe",
            "john@example.com",
            "password",
            UserRole.STUDENT,
            specialtyId,
            2025);

    ResponseEntity<User> createResponse =
        restTemplate.postForEntity("/users", json(createRequest), User.class);

    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    assertNotNull(createResponse.getBody());
    UUID userId = createResponse.getBody().getId();

    ResponseEntity<UserPage> listResponse =
        restTemplate.getForEntity(
            "/users?page=0&pageSize=20&firstName=ohN&email=EXAMPLE", UserPage.class);
    assertEquals(HttpStatus.OK, listResponse.getStatusCode());
    assertEquals(1, listResponse.getBody().getContent().size());

    ResponseEntity<User> getResponse =
        restTemplate.getForEntity("/users/{userId}", User.class, userId);
    assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    assertEquals(userId, getResponse.getBody().getId());

    UserUpdate updateRequest =
        new UserUpdate(null, "Updated", null, null, "new-password", null, null, null);
    ResponseEntity<User> updateResponse =
        restTemplate.exchange(
            "/users/{userId}",
            HttpMethod.PATCH,
            json(updateRequest),
            User.class,
            userId);
    assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
    assertEquals("Updated", updateResponse.getBody().getFirstName());

    ResponseEntity<Void> deleteResponse =
        restTemplate.exchange(
            "/users/{userId}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, userId);
    assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

    ResponseEntity<String> missingResponse =
        restTemplate.getForEntity("/users/{userId}", String.class, userId);
    assertEquals(HttpStatus.NOT_FOUND, missingResponse.getStatusCode());
  }

  @Test
  void shouldReturnConflictForDuplicateReferenceAndEmail() {
    UUID specialtyId = specialtyId();
    UserCreate firstRequest =
        new UserCreate(
            "REF001", "John", "Doe", "john@example.com", "password", UserRole.STUDENT,
            specialtyId, 2025);
    restTemplate.postForEntity("/users", json(firstRequest), User.class);

    UserCreate duplicateReference =
        new UserCreate(
            "REF001", "Jane", "Doe", "jane@example.com", "password", UserRole.STUDENT,
            specialtyId, 2025);
    ResponseEntity<String> referenceResponse =
        restTemplate.postForEntity("/users", json(duplicateReference), String.class);
    assertEquals(HttpStatus.CONFLICT, referenceResponse.getStatusCode());

    UserCreate duplicateEmail =
        new UserCreate(
            "REF002", "Jane", "Doe", "JOHN@EXAMPLE.COM", "password", UserRole.STUDENT,
            specialtyId, 2025);
    ResponseEntity<String> emailResponse =
        restTemplate.postForEntity("/users", json(duplicateEmail), String.class);
    assertEquals(HttpStatus.CONFLICT, emailResponse.getStatusCode());
  }

  @Test
  void shouldReturnBadRequestForInvalidUuidAndEmptyBody() {
    ResponseEntity<String> invalidIdResponse =
        restTemplate.getForEntity("/users/not-a-uuid", String.class);
    assertEquals(HttpStatus.BAD_REQUEST, invalidIdResponse.getStatusCode());

    ResponseEntity<String> emptyBodyResponse =
        restTemplate.postForEntity("/users", new HttpEntity<>(null, headers()), String.class);
    assertEquals(HttpStatus.BAD_REQUEST, emptyBodyResponse.getStatusCode());
  }

  private UUID specialtyId() {
    return jdbcTemplate.queryForObject("select id from specialty where code = 'NONE'", UUID.class);
  }

  private <T> HttpEntity<T> json(T body) {
    return new HttpEntity<>(body, headers());
  }

  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
