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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.mappers.ExamMapper;
import school.hei.demo.entity.Exam;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JExam;
import school.hei.demo.validators.ExamValidator;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {
  @Mock ExamRepository repository;
  @Mock ExamMapper mapper;
  @Mock ExamValidator validator;
  @InjectMocks ExamService service;

  @Test
  void shouldListExamsForCourse() {
    UUID courseId = UUID.randomUUID();
    JExam entity =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(JCourse.builder().id(courseId).build())
            .build();
    Exam exam = new Exam(entity.getId(), courseId, Instant.now(), BigDecimal.ONE);
    when(repository.findAllByCourse_Id(courseId)).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(exam);

    assertEquals(List.of(exam), service.listForCourse(courseId));
  }

  @Test
  void shouldCreateUpdateAndDeleteExam() {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    Exam request = new Exam(null, null, Instant.now(), BigDecimal.valueOf(0.5));
    Exam saved = new Exam(examId, courseId, request.getDateExam(), request.getCoef());
    JExam entity =
        JExam.builder().id(examId).course(JCourse.builder().id(courseId).build()).build();
    when(mapper.toEntity(any(Exam.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(saved);
    when(repository.findById(examId)).thenReturn(Optional.of(entity));

    assertEquals(saved, service.create(courseId, request));
    assertEquals(saved, service.update(courseId, examId, request));
    service.delete(courseId, examId);
    verify(repository).deleteById(examId);
  }

  @Test
  void shouldRejectUnknownOrWrongCourseExam() {
    UUID courseId = UUID.randomUUID();
    UUID otherCourseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
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

  private Exam validExam() {
    return new Exam(null, null, Instant.now(), BigDecimal.ONE);
  }
}
