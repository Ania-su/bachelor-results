package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.mappers.CourseAssignmentMapper;
import school.hei.demo.entity.CourseAssignment;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseAssignmentRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JCourseAssignment;
import school.hei.demo.repository.model.JGroup;
import school.hei.demo.validators.CourseAssignmentValidator;

@ExtendWith(MockitoExtension.class)
class CourseAssignmentServiceTest {
  @Mock CourseAssignmentRepository repository;
  @Mock CourseAssignmentMapper mapper;
  @Mock CourseAssignmentValidator validator;
  @InjectMocks CourseAssignmentService service;

  @Test
  void shouldListAssignmentsForCourse() {
    UUID courseId = UUID.randomUUID();
    JCourseAssignment entity = JCourseAssignment.builder().id(UUID.randomUUID()).build();
    CourseAssignment assignment =
        new CourseAssignment(entity.getId(), courseId, UUID.randomUUID(), UUID.randomUUID());
    when(repository.findAllByCourse_Id(courseId)).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(assignment);

    assertEquals(List.of(assignment), service.listForCourse(courseId));
  }

  @Test
  void shouldCreateUpdateAndDeleteAssignment() {
    UUID courseId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();
    CourseAssignment request =
        new CourseAssignment(null, null, UUID.randomUUID(), UUID.randomUUID());
    CourseAssignment saved =
        new CourseAssignment(assignmentId, courseId, request.getTeacherId(), request.getGroupId());
    JCourseAssignment entity =
        JCourseAssignment.builder()
            .id(assignmentId)
            .course(JCourse.builder().id(courseId).build())
            .group(JGroup.builder().id(request.getGroupId()).build())
            .teacherId(request.getTeacherId())
            .build();
    when(mapper.toEntity(any(CourseAssignment.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(saved);
    when(repository.findById(assignmentId)).thenReturn(Optional.of(entity));

    assertEquals(saved, service.create(courseId, request));
    assertEquals(saved, service.update(courseId, assignmentId, request));
    service.delete(courseId, assignmentId);
    verify(repository).deleteById(assignmentId);
  }

  @Test
  void shouldRejectUnknownOrWrongCourseAssignment() {
    UUID courseId = UUID.randomUUID();
    UUID otherCourseId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();
    when(repository.findById(assignmentId)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.get(courseId, assignmentId));
    assertThrows(
        NotFoundException.class, () -> service.update(courseId, assignmentId, validAssignment()));

    JCourseAssignment entity = JCourseAssignment.builder().id(assignmentId).build();
    when(repository.findById(assignmentId)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity))
        .thenReturn(
            new CourseAssignment(
                assignmentId, otherCourseId, UUID.randomUUID(), UUID.randomUUID()));
    assertThrows(NotFoundException.class, () -> service.get(courseId, assignmentId));
    assertThrows(NotFoundException.class, () -> service.delete(courseId, assignmentId));
  }

  private CourseAssignment validAssignment() {
    return new CourseAssignment(null, null, UUID.randomUUID(), UUID.randomUUID());
  }
}
