package school.hei.demo.IT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.demo.conf.FacadeIT;
import school.hei.demo.conf.TestAuthentication;
import school.hei.demo.domain.dto.request.LoginRequest;
import school.hei.demo.domain.dto.response.AuthResponse;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.endpoint.event.EventProducer;
import school.hei.demo.endpoint.event.model.SendEmailRequested;
import school.hei.demo.enums.UserRole;
import school.hei.demo.file.bucket.BucketComponent;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JExam;
import school.hei.demo.repository.model.JGrade;
import school.hei.demo.repository.model.JUser;

class TranscriptIT extends FacadeIT {
  private static final String STUDENT_PASSWORD = "student-password";

  @Autowired TestRestTemplate restTemplate;
  @Autowired SpecialtyRepository specialtyRepository;
  @Autowired CourseRepository courseRepository;
  @Autowired ExamRepository examRepository;
  @Autowired UserRepository userRepository;
  @Autowired GradeRepository gradeRepository;
  @Autowired PasswordEncoder passwordEncoder;

  @MockBean BucketComponent bucketComponent;
  @MockBean EventProducer<SendEmailRequested> eventProducer;

  @BeforeEach
  void setUpClient() {
    restTemplate
        .getRestTemplate()
        .setRequestFactory(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
  }

  @Test
  void admin_canGetStudentTranscriptPojoWithoutEmailing() {
    JUser student = createStudentWithGrades("TRSTD01", "Pojo", "Student", "pojo@hei.mg", "TRSCR1");

    TestAuthentication.configure(
        restTemplate, userRepository, specialtyRepository, passwordEncoder);

    ResponseEntity<StudentTranscriptResponse> response =
        restTemplate.getForEntity(
            "/students/" + student.getId() + "/transcript", StudentTranscriptResponse.class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals("TRSTD01", response.getBody().getUser().getReference());
    assertEquals(1, response.getBody().getYears().size());
    verifyNoInteractions(bucketComponent);
    verifyNoInteractions(eventProducer);
  }

  @Test
  void student_requestingTranscriptByEmail_getsUploadedPdfAndEmailEvent() throws Exception {
    JUser student = createStudentWithGrades("TRSTD02", "Mail", "Student", "mail@hei.mg", "TRSCR2");
    loginAs(student.getEmail(), STUDENT_PASSWORD);
    var presignedUrl =
        URI.create(
                "https://fake-bucket.s3.amazonaws.com/transcripts/"
                    + student.getId()
                    + ".pdf?signature=abc")
            .toURL();
    when(bucketComponent.presign(anyString(), any(Duration.class))).thenReturn(presignedUrl);

    ResponseEntity<String> response =
        restTemplate.getForEntity("/me/transcript-email", String.class);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Check your email for your requested transcript.", response.getBody());

    var fileCaptor = ArgumentCaptor.forClass(File.class);
    verify(bucketComponent).upload(fileCaptor.capture(), anyString());
    var uploadedFile = fileCaptor.getValue();
    try {
      var content =
          new String(Files.readAllBytes(uploadedFile.toPath()), StandardCharsets.ISO_8859_1);
      assertTrue(content.startsWith("%PDF"), "uploaded file should be a valid PDF");
    } finally {
      uploadedFile.delete();
    }
    verify(bucketComponent).presign(anyString(), any(Duration.class));

    @SuppressWarnings({"unchecked"})
    ArgumentCaptor<Collection<SendEmailRequested>> eventCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(eventProducer).accept(eventCaptor.capture());
    var event = eventCaptor.getValue().iterator().next();
    assertEquals(student.getEmail(), event.getTo());
    assertEquals(student.getReference() + " transcript", event.getSubject());
    assertTrue(event.getBody().contains(presignedUrl.toString()));
  }

  private JUser createStudentWithGrades(
      String reference, String firstName, String lastName, String email, String courseRef) {
    var specialty = specialtyRepository.findAll().get(0);
    JUser student = new JUser();
    student.setReference(reference);
    student.setFirstName(firstName);
    student.setLastName(lastName);
    student.setEmail(email);
    student.setPassword(passwordEncoder.encode(STUDENT_PASSWORD));
    student.setUserRole(UserRole.STUDENT);
    student.setSpecialtyId(specialty.getId());
    student.setEntryYear(2024);
    student = userRepository.save(student);

    JCourse course =
        courseRepository.save(
            JCourse.builder()
                .reference(courseRef)
                .title("Cours Transcript Test")
                .semester(1)
                .credits(5)
                .build());

    JExam exam =
        examRepository.save(
            JExam.builder()
                .course(course)
                .dateExam(Instant.parse("2026-01-15T10:00:00Z"))
                .coef(new BigDecimal("1.00"))
                .build());

    gradeRepository.save(
        JGrade.builder()
            .student(student)
            .exam(exam)
            .value(new BigDecimal("15.00"))
            .updatedAt(Instant.now())
            .build());

    return student;
  }

  private void loginAs(String email, String password) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<AuthResponse> loginResponse =
        restTemplate.postForEntity(
            "/auth/login",
            new HttpEntity<>(new LoginRequest(email, password), headers),
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
}
