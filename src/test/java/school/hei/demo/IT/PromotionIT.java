package school.hei.demo.IT;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.demo.conf.FacadeIT;
import school.hei.demo.conf.TestAuthentication;
import school.hei.demo.domain.dto.response.GraduateResponse;
import school.hei.demo.domain.dto.response.PromotionDownloadResponse;
import school.hei.demo.enums.CodeType;
import school.hei.demo.enums.UserRole;
import school.hei.demo.file.bucket.BucketComponent;
import school.hei.demo.repository.*;
import school.hei.demo.repository.model.*;

class PromotionIT extends FacadeIT {

  @Autowired TestRestTemplate restTemplate;

  @Autowired SpecialtyRepository specialtyRepository;
  @Autowired CourseRepository courseRepository;
  @Autowired CourseSpecialtyRepository courseSpecialtyRepository;
  @Autowired ExamRepository examRepository;
  @Autowired UserRepository userRepository;
  @Autowired GradeRepository gradeRepository;

  @Autowired GroupRepository groupRepository;
  @Autowired CourseAssignmentRepository courseAssignmentRepository;
  @Autowired StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Autowired PasswordEncoder passwordEncoder;

  @MockBean BucketComponent bucketComponent;

  @BeforeEach
  void setUpClient() {
    restTemplate
        .getRestTemplate()
        .setRequestFactory(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));
    TestAuthentication.configure(
        restTemplate, userRepository, specialtyRepository, passwordEncoder);
  }

  @Test
  void shouldListOnlyStudentsWithAllCourseAveragesAboveOrEqualTen() {
    int year = 2024;

    JSpecialty specialty =
        specialtyRepository.findAll().stream()
            .filter(s -> s.getCode() == CodeType.EL)
            .findFirst()
            .orElseThrow();

    JGroup group =
        groupRepository.save(JGroup.builder().reference("A1").academicYear(year).build());

    JCourse course =
        courseRepository.save(
            JCourse.builder()
                .reference("PROMO1")
                .title("Cours Promotion Test")
                .semester(1)
                .credits(5)
                .build());

    courseSpecialtyRepository.save(
        JCourseSpecialty.builder().course(course).specialty(specialty).build());

    JUser teacher =
        createTeacher(
            "TEA24001", "Teacher", "Test", "teacher-promo1@hei.mg", specialty.getId(), year);

    courseAssignmentRepository.save(
        JCourseAssignment.builder().course(course).group(group).teacherId(teacher.getId()).build());

    JExam exam =
        examRepository.save(
            JExam.builder()
                .course(course)
                .dateExam(Instant.parse("2026-01-15T10:00:00Z"))
                .coef(new BigDecimal("1.00"))
                .build());

    JUser graduateStudent =
        createStudent("STD24001", "Grad", "One", "grad1@hei.mg", specialty.getId(), year);

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(graduateStudent)
            .group(group)
            .startDate(LocalDate.of(year, 1, 1))
            .build());

    gradeRepository.save(
        JGrade.builder()
            .student(graduateStudent)
            .exam(exam)
            .value(new BigDecimal("15.00"))
            .updatedAt(Instant.now())
            .build());

    JUser failingStudent =
        createStudent("STD24002", "Fail", "One", "fail1@hei.mg", specialty.getId(), year);

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(failingStudent)
            .group(group)
            .startDate(LocalDate.of(year, 1, 1))
            .build());

    gradeRepository.save(
        JGrade.builder()
            .student(failingStudent)
            .exam(exam)
            .value(new BigDecimal("8.00"))
            .updatedAt(Instant.now())
            .build());

    JUser noGradeStudent =
        createStudent("STD24003", "NoGrade", "One", "nograde1@hei.mg", specialty.getId(), year);

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(noGradeStudent)
            .group(group)
            .startDate(LocalDate.of(year, 1, 1))
            .build());

    ResponseEntity<GraduateResponse[]> response =
        restTemplate.getForEntity("/promotions/" + year + "/graduates", GraduateResponse[].class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().length);

    assertEquals("STD24001", response.getBody()[0].getReference());
    assertEquals(1, response.getBody()[0].getRank());
    assertEquals(0, new BigDecimal("15.00").compareTo(response.getBody()[0].getAverage()));
  }

  @Test
  void shouldRankGraduatesByDescendingAverage() {

    int year = 2025;

    JSpecialty specialty =
        specialtyRepository.findAll().stream()
            .filter(s -> s.getCode() == CodeType.TN)
            .findFirst()
            .orElseThrow();

    JGroup group =
        groupRepository.save(JGroup.builder().reference("A2").academicYear(year).build());

    JCourse course =
        courseRepository.save(
            JCourse.builder()
                .reference("PROMO2")
                .title("Cours Rang Test")
                .semester(1)
                .credits(5)
                .build());

    courseSpecialtyRepository.save(
        JCourseSpecialty.builder().course(course).specialty(specialty).build());

    JUser teacher =
        createTeacher(
            "TEA25001", "Teacher", "Test", "teacher-promo2@hei.mg", specialty.getId(), year);

    courseAssignmentRepository.save(
        JCourseAssignment.builder().course(course).group(group).teacherId(teacher.getId()).build());

    JExam exam =
        examRepository.save(
            JExam.builder()
                .course(course)
                .dateExam(Instant.parse("2026-01-15T10:00:00Z"))
                .coef(new BigDecimal("1.00"))
                .build());

    JUser first =
        createStudent("STD25010", "Best", "Student", "best@hei.mg", specialty.getId(), year);

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(first)
            .group(group)
            .startDate(LocalDate.of(year, 1, 1))
            .build());

    gradeRepository.save(
        JGrade.builder()
            .student(first)
            .exam(exam)
            .value(new BigDecimal("18.00"))
            .updatedAt(Instant.now())
            .build());

    JUser second =
        createStudent("STD25011", "Second", "Student", "second@hei.mg", specialty.getId(), year);

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(second)
            .group(group)
            .startDate(LocalDate.of(year, 1, 1))
            .build());

    gradeRepository.save(
        JGrade.builder()
            .student(second)
            .exam(exam)
            .value(new BigDecimal("12.00"))
            .updatedAt(Instant.now())
            .build());

    ResponseEntity<GraduateResponse[]> response =
        restTemplate.getForEntity("/promotions/" + year + "/graduates", GraduateResponse[].class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().length);

    assertEquals("STD25010", response.getBody()[0].getReference());
    assertEquals(1, response.getBody()[0].getRank());

    assertEquals("STD25011", response.getBody()[1].getReference());
    assertEquals(2, response.getBody()[1].getRank());
  }

  @Test
  void shouldGenerateExcelDownloadUrlWithMockedBucket() throws Exception {
    int year = 2026;

    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(
            URI.create("https://fake-bucket.s3.amazonaws.com/graduates.xlsx?signature=abc")
                .toURL());

    ResponseEntity<PromotionDownloadResponse> response =
        restTemplate.getForEntity(
            "/promotions/" + year + "/graduates/download", PromotionDownloadResponse.class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getDownloadUrl());
    assertTrue(response.getBody().getDownloadUrl().contains("graduates.xlsx"));
    verify(bucketComponent).upload(any(), anyString());
    verify(bucketComponent).presign(anyString(), any(Duration.class));
  }

  @Test
  void shouldReturnEmptyListForYearWithNoStudents() {
    ResponseEntity<GraduateResponse[]> response =
        restTemplate.getForEntity("/promotions/1999/graduates", GraduateResponse[].class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().length);
  }

  private JUser createStudent(
      String reference,
      String firstName,
      String lastName,
      String email,
      UUID specialtyId,
      int entryYear) {
    JUser user = new JUser();
    user.setReference(reference);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPassword("hashed-password");
    user.setUserRole(UserRole.STUDENT);
    user.setSpecialtyId(specialtyId);
    user.setEntryYear(entryYear);
    return userRepository.save(user);
  }

  private JUser createTeacher(
      String reference,
      String firstName,
      String lastName,
      String email,
      UUID specialtyId,
      int entryYear) {

    JUser teacher = new JUser();
    teacher.setReference(reference);
    teacher.setFirstName(firstName);
    teacher.setLastName(lastName);
    teacher.setEmail(email);
    teacher.setPassword("hashed-password");
    teacher.setUserRole(UserRole.TEACHER);
    teacher.setSpecialtyId(specialtyId);
    teacher.setEntryYear(entryYear);

    return userRepository.save(teacher);
  }
}
