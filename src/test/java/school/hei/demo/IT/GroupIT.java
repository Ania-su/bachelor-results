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
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.demo.conf.FacadeIT;
import school.hei.demo.conf.TestAuthentication;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupPage;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.repository.UserRepository;

class GroupIT extends FacadeIT {
  @Autowired TestRestTemplate restTemplate;
  @Autowired GroupRepository repository;
  @Autowired UserRepository userRepository;
  @Autowired SpecialtyRepository specialtyRepository;
  @Autowired PasswordEncoder passwordEncoder;

  @BeforeEach
  void cleanGroups() {
    repository.deleteAll();
    restTemplate
        .getRestTemplate()
        .setRequestFactory(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
    TestAuthentication.configure(
        restTemplate, userRepository, specialtyRepository, passwordEncoder);
  }

  @Test
  void shouldCompleteCrudAndFilterThroughHttp() {
    GroupRequest request = new GroupRequest("A1", 2025);
    ResponseEntity<GroupResponse> created =
        restTemplate.postForEntity("/groups", json(request), GroupResponse.class);
    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    UUID id = created.getBody().getId();

    ResponseEntity<GroupPage> list =
        restTemplate.getForEntity("/groups?page=0&pageSize=20&academicYear=2025", GroupPage.class);
    assertEquals(HttpStatus.OK, list.getStatusCode());
    assertEquals(1, list.getBody().getContent().size());

    ResponseEntity<GroupResponse> updated =
        restTemplate.exchange(
            "/groups/{id}",
            HttpMethod.PATCH,
            json(new GroupRequest("B1", 2026)),
            GroupResponse.class,
            id);
    assertEquals(HttpStatus.OK, updated.getStatusCode());
    assertEquals("B1", updated.getBody().getReference());
    assertEquals(
        HttpStatus.NO_CONTENT,
        restTemplate
            .exchange("/groups/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, id)
            .getStatusCode());
    assertEquals(
        HttpStatus.NOT_FOUND,
        restTemplate.getForEntity("/groups/{id}", String.class, id).getStatusCode());
  }

  @Test
  void shouldRejectInvalidInputAndUnknownId() {
    assertEquals(
        HttpStatus.BAD_REQUEST,
        restTemplate
            .postForEntity("/groups", json(new GroupRequest("ABC", 2025)), String.class)
            .getStatusCode());
    assertEquals(
        HttpStatus.BAD_REQUEST,
        restTemplate.getForEntity("/groups/not-a-uuid", String.class).getStatusCode());
  }

  private <T> HttpEntity<T> json(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }
}
