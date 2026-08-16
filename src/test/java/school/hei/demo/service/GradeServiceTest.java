package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.dto.request.GradeCreate;
import school.hei.demo.domain.dto.request.GradeUpdate;
import school.hei.demo.domain.mappers.GradeMapper;
import school.hei.demo.endpoint.rest.controller.mapper.GradeRestMapper;
import school.hei.demo.entity.Grade;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.GradeHistoryRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JExam;
import school.hei.demo.repository.model.JGrade;
import school.hei.demo.repository.model.JGradeHistory;
import school.hei.demo.validators.GradeValidator;
import school.hei.demo.validators.PaginationValidator;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {
  private static final UUID COURSE_ID = UUID.randomUUID();
  private static final UUID EXAM_ID = UUID.randomUUID();
  private static final UUID STUDENT_ID = UUID.randomUUID();
  private static final UUID GRADE_ID = UUID.randomUUID();

  @Mock GradeRepository repository;
  @Mock GradeHistoryRepository historyRepository;
  @Mock ExamRepository examRepository;
  @Mock UserRepository userRepository;
  @Mock GradeMapper mapper;
  @Mock GradeRestMapper restMapper;
  @Mock GradeValidator validator;
  @Mock PaginationValidator paginationValidator;
  @InjectMocks GradeService service;

  @Test
  void shouldCreateGradeAndInitialHistory() {
    var request = new GradeCreate(STUDENT_ID, BigDecimal.valueOf(14.5));
    var grade =
        Grade.builder().studentId(STUDENT_ID).examId(EXAM_ID).value(request.getValue()).build();
    var savedEntity = JGrade.builder().id(GRADE_ID).build();
    var savedGrade = grade.toBuilder().id(GRADE_ID).updatedAt(Instant.now()).build();
    var course = JCourse.builder().id(COURSE_ID).build();
    var exam = JExam.builder().id(EXAM_ID).course(course).build();

    when(examRepository.findById(EXAM_ID)).thenReturn(Optional.of(exam));
    when(userRepository.existsById(STUDENT_ID)).thenReturn(true);
    when(repository.existsByStudent_IdAndExam_Id(STUDENT_ID, EXAM_ID)).thenReturn(false);
    when(restMapper.toDomain(request)).thenReturn(grade);
    when(mapper.toEntity(any(Grade.class))).thenReturn(savedEntity);
    when(repository.save(savedEntity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(savedGrade);

    service.create(COURSE_ID, EXAM_ID, request);

    var history = ArgumentCaptor.forClass(JGradeHistory.class);
    verify(historyRepository).save(history.capture());
    assertEquals(GRADE_ID, history.getValue().getGradeId());
    assertEquals(BigDecimal.ZERO, history.getValue().getOldValue());
    assertEquals(request.getValue(), history.getValue().getNewValue());
    assertEquals("Initial grade", history.getValue().getReason());
    assertEquals(
        UUID.fromString("2d4149bf-c264-464e-b6a4-a364275df2ec"), history.getValue().getChangedBy());
  }

  @Test
  void shouldRejectDuplicateGrade() {
    var request = new GradeCreate(STUDENT_ID, BigDecimal.TEN);
    var grade = Grade.builder().studentId(STUDENT_ID).value(BigDecimal.TEN).build();
    var course = JCourse.builder().id(COURSE_ID).build();
    var exam = JExam.builder().id(EXAM_ID).course(course).build();
    when(examRepository.findById(EXAM_ID)).thenReturn(Optional.of(exam));
    when(userRepository.existsById(STUDENT_ID)).thenReturn(true);
    when(repository.existsByStudent_IdAndExam_Id(STUDENT_ID, EXAM_ID)).thenReturn(true);
    when(restMapper.toDomain(request)).thenReturn(grade);

    assertThrows(BadRequestException.class, () -> service.create(COURSE_ID, EXAM_ID, request));
  }

  @Test
  void shouldUpdateAndAuditGrade() {
    var current = Grade.builder().id(GRADE_ID).examId(EXAM_ID).value(BigDecimal.TEN).build();
    var entity = JGrade.builder().id(GRADE_ID).build();
    var updated = current.toBuilder().value(BigDecimal.valueOf(12)).build();
    var course = JCourse.builder().id(COURSE_ID).build();
    var exam = JExam.builder().id(EXAM_ID).course(course).build();
    when(repository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(current, updated);
    when(examRepository.findById(EXAM_ID)).thenReturn(Optional.of(exam));
    when(mapper.toEntity(any(Grade.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);

    service.update(
        COURSE_ID, EXAM_ID, GRADE_ID, new GradeUpdate(BigDecimal.valueOf(12), "Correction"));

    verify(historyRepository).save(any(JGradeHistory.class));
    verify(repository).save(entity);
  }

  @Test
  void shouldDeleteHistoryThenGradeAndReturnDeletedGrade() {
    var grade = Grade.builder().id(GRADE_ID).examId(EXAM_ID).value(BigDecimal.TEN).build();
    var entity = JGrade.builder().id(GRADE_ID).build();
    when(repository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(grade);

    service.delete(GRADE_ID);

    var inOrder = org.mockito.Mockito.inOrder(historyRepository, repository);
    inOrder.verify(historyRepository).deleteAllByGradeId(GRADE_ID);
    inOrder.verify(repository).deleteById(GRADE_ID);
  }

  @Test
  void shouldRejectDeletingUnknownGrade() {
    when(repository.findById(GRADE_ID)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.delete(GRADE_ID));
  }
}
