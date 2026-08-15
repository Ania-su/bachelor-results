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
import school.hei.demo.conf.FacadeIT;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.domain.dto.response.CoursePage;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.repository.CourseRepository;

class CourseIT extends FacadeIT {
  @Autowired TestRestTemplate restTemplate;
  @Autowired CourseRepository repository;

  @BeforeEach
  void cleanCourses() {
    repository.deleteAll();
    restTemplate
        .getRestTemplate()
        .setRequestFactory(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
  }

  @Test
  void shouldCompleteCrudAndFilterThroughHttp() {
    CourseRequest request = new CourseRequest("CS", "Algorithms", 2, 6);
    ResponseEntity<CourseResponse> created =
        restTemplate.postForEntity("/courses", json(request), CourseResponse.class);
    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    UUID id = created.getBody().getId();

    ResponseEntity<CoursePage> list =
        restTemplate.getForEntity(
            "/courses?page=0&pageSize=20&semester=2&search=algo", CoursePage.class);
    assertEquals(HttpStatus.OK, list.getStatusCode());
    assertEquals(1, list.getBody().getContent().size());

    ResponseEntity<CourseResponse> updated =
        restTemplate.exchange(
            "/courses/{id}",
            HttpMethod.PATCH,
            json(new CourseRequest("MA", "Math", 1, 4)),
            CourseResponse.class,
            id);
    assertEquals(HttpStatus.OK, updated.getStatusCode());
    assertEquals("Math", updated.getBody().getTitle());
    assertEquals(
        HttpStatus.NO_CONTENT,
        restTemplate
            .exchange("/courses/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, id)
            .getStatusCode());
    assertEquals(
        HttpStatus.NOT_FOUND,
        restTemplate.getForEntity("/courses/{id}", String.class, id).getStatusCode());
  }

  @Test
  void shouldRejectInvalidInputAndUnknownId() {
    assertEquals(
        HttpStatus.BAD_REQUEST,
        restTemplate
            .postForEntity(
                "/courses", json(new CourseRequest("CS", "Algorithms", 7, 6)), String.class)
            .getStatusCode());
    assertEquals(
        HttpStatus.BAD_REQUEST,
        restTemplate.getForEntity("/courses/not-a-uuid", String.class).getStatusCode());
  }

  private <T> HttpEntity<T> json(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }
}
