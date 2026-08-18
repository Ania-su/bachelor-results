package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.dto.request.ExamRequest;
import school.hei.demo.domain.dto.response.ExamResponse;
import school.hei.demo.domain.mappers.ExamMapper;
import school.hei.demo.endpoint.rest.controller.mapper.ExamRestMapper;
import school.hei.demo.entity.Exam;
import school.hei.demo.entity.User;
import school.hei.demo.enums.CodeType;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.ForbiddenException;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseAssignmentRepository;
import school.hei.demo.repository.CourseSpecialtyRepository;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JCourseSpecialty;
import school.hei.demo.repository.model.JExam;
import school.hei.demo.repository.model.JSpecialty;
import school.hei.demo.validators.ExamValidator;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {
  @Mock ExamRepository repository;
  @Mock ExamMapper mapper;
  @Mock ExamValidator validator;
  @Mock CurrentUserService currentUserService;
  @Mock CourseAssignmentRepository courseAssignmentRepository;
  @Mock CourseSpecialtyRepository courseSpecialtyRepository;
  @Mock SpecialtyRepository specialtyRepository;
  @Mock ExamRestMapper restMapper;
  @InjectMocks ExamService service;

  @BeforeEach
  void setUpAuthenticatedAdmin() {
    var admin = new User();
    admin.setId(UUID.randomUUID());
    admin.setUserRole(UserRole.ADMIN);
    when(currentUserService.getCurrentUser()).thenReturn(admin);
  }

  @Test
  void shouldListExamsForCourse() {
    UUID courseId = UUID.randomUUID();
    JExam entity =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(JCourse.builder().id(courseId).build())
            .build();
    Exam exam = new Exam(entity.getId(), courseId, Instant.now(), BigDecimal.ONE);
    ExamResponse response =
        new ExamResponse(entity.getId(), courseId, exam.getDateExam(), BigDecimal.ONE);
    when(repository.findAllByCourse_Id(courseId)).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(exam);
    when(restMapper.toResponse(exam)).thenReturn(response);

    assertEquals(List.of(response), service.listForCourse(courseId));
  }

  @Test
  void shouldCreateUpdateAndDeleteExam() {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    ExamRequest request = new ExamRequest(Instant.now(), BigDecimal.valueOf(0.5));
    Exam domain = new Exam(null, null, request.getDateExam(), request.getCoef());
    Exam saved = new Exam(examId, courseId, request.getDateExam(), request.getCoef());
    ExamResponse savedResponse =
        new ExamResponse(examId, courseId, request.getDateExam(), request.getCoef());
    JExam entity =
        JExam.builder().id(examId).course(JCourse.builder().id(courseId).build()).build();
    when(restMapper.toDomain(request)).thenReturn(domain);
    when(mapper.toEntity(any(Exam.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(saved);
    when(restMapper.toResponse(saved)).thenReturn(savedResponse);
    when(repository.findById(examId)).thenReturn(Optional.of(entity));

    assertEquals(savedResponse, service.create(courseId, request));
    assertEquals(savedResponse, service.update(courseId, examId, request));
    service.delete(courseId, examId);
    verify(repository).deleteById(examId);
  }

  @Test
  void shouldRejectUnknownOrWrongCourseExam() {
    UUID courseId = UUID.randomUUID();
    UUID otherCourseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    when(restMapper.toDomain(any(ExamRequest.class)))
        .thenReturn(new Exam(null, null, Instant.now(), BigDecimal.ONE));
    when(repository.findById(examId)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.get(courseId, examId));
    assertThrows(NotFoundException.class, () -> service.update(courseId, examId, validExam()));

    JExam entity = JExam.builder().id(examId).build();
    when(repository.findById(examId)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity))
        .thenReturn(new Exam(examId, otherCourseId, Instant.now(), BigDecimal.ONE));
    assertThrows(NotFoundException.class, () -> service.get(courseId, examId));
    assertThrows(NotFoundException.class, () -> service.delete(courseId, examId));
  }

  @Test
  void shouldRejectUnassignedTeacher() {
    UUID courseId = UUID.randomUUID();
    var teacher = new User();
    teacher.setId(UUID.randomUUID());
    teacher.setUserRole(UserRole.TEACHER);
    when(currentUserService.getCurrentUser()).thenReturn(teacher);
    when(courseAssignmentRepository.existsByCourse_IdAndTeacherId(courseId, teacher.getId()))
        .thenReturn(false);

    assertThrows(ForbiddenException.class, () -> service.listForCourse(courseId));
  }

  @Test
  void shouldFilterStudentExamsBySpecialtyAndEntryYear() {
    UUID courseId = UUID.randomUUID();
    UUID specialtyId = UUID.randomUUID();
    var student = new User();
    student.setId(UUID.randomUUID());
    student.setUserRole(UserRole.STUDENT);
    student.setSpecialtyId(specialtyId);
    student.setEntryYear(2024);
    when(currentUserService.getCurrentUser()).thenReturn(student);
    when(specialtyRepository.findById(specialtyId))
        .thenReturn(Optional.of(JSpecialty.builder().id(specialtyId).code(CodeType.EL).build()));
    when(courseSpecialtyRepository.findByCourseId(courseId))
        .thenReturn(
            List.of(
                JCourseSpecialty.builder()
                    .course(JCourse.builder().id(courseId).build())
                    .specialty(JSpecialty.builder().code(CodeType.NONE).build())
                    .build()));

    JExam allowedEntity =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(JCourse.builder().id(courseId).build())
            .build();
    JExam oldEntity =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(JCourse.builder().id(courseId).build())
            .build();
    Exam allowed =
        new Exam(
            allowedEntity.getId(), courseId, Instant.parse("2024-01-01T00:00:00Z"), BigDecimal.ONE);
    Exam old =
        new Exam(
            oldEntity.getId(), courseId, Instant.parse("2023-12-31T23:59:59Z"), BigDecimal.ONE);
    ExamResponse allowedResponse =
        new ExamResponse(allowedEntity.getId(), courseId, allowed.getDateExam(), BigDecimal.ONE);
    when(repository.findAllByCourse_Id(courseId)).thenReturn(List.of(allowedEntity, oldEntity));
    when(mapper.toDomain(allowedEntity)).thenReturn(allowed);
    when(mapper.toDomain(oldEntity)).thenReturn(old);
    when(restMapper.toResponse(allowed)).thenReturn(allowedResponse);

    assertEquals(List.of(allowedResponse), service.listForCourse(courseId));
  }

  private ExamRequest validExam() {
    return new ExamRequest(Instant.now(), BigDecimal.ONE);
  }
}
