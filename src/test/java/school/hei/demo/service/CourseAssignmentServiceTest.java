package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.dto.request.CourseAssignmentRequest;
import school.hei.demo.domain.dto.response.CourseAssignmentResponse;
import school.hei.demo.domain.mappers.CourseAssignmentMapper;
import school.hei.demo.endpoint.rest.controller.mapper.CourseAssignmentRestMapper;
import school.hei.demo.entity.CourseAssignment;
import school.hei.demo.entity.User;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.ForbiddenException;
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
  @Mock CurrentUserService currentUserService;
  @Mock CourseAssignmentRestMapper restMapper;
  @InjectMocks CourseAssignmentService service;

  @BeforeEach
  void setUpAuthenticatedAdmin() {
    var admin = new User();
    admin.setId(UUID.randomUUID());
    admin.setUserRole(UserRole.ADMIN);
    when(currentUserService.getCurrentUser()).thenReturn(admin);
  }

  @Test
  void shouldListAssignmentsForCourse() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    JCourseAssignment entity = JCourseAssignment.builder().id(UUID.randomUUID()).build();
    CourseAssignment assignment =
        new CourseAssignment(entity.getId(), courseId, teacherId, groupId);
    CourseAssignmentResponse response =
        new CourseAssignmentResponse(entity.getId(), courseId, teacherId, groupId);
    when(repository.findAllByCourse_Id(courseId)).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(assignment);
    when(restMapper.toResponse(assignment)).thenReturn(response);

    assertEquals(List.of(response), service.listForCourse(courseId));
  }

  @Test
  void shouldCreateUpdateAndDeleteAssignment() {
    UUID courseId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    CourseAssignmentRequest request = new CourseAssignmentRequest(teacherId, groupId);
    CourseAssignment domain = new CourseAssignment(null, null, teacherId, groupId);
    CourseAssignment saved = new CourseAssignment(assignmentId, courseId, teacherId, groupId);
    CourseAssignmentResponse savedResponse =
        new CourseAssignmentResponse(assignmentId, courseId, teacherId, groupId);
    JCourseAssignment entity =
        JCourseAssignment.builder()
            .id(assignmentId)
            .course(JCourse.builder().id(courseId).build())
            .group(JGroup.builder().id(groupId).build())
            .teacherId(teacherId)
            .build();
    when(restMapper.toDomain(request)).thenReturn(domain);
    when(mapper.toEntity(any(CourseAssignment.class))).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(saved);
    when(restMapper.toResponse(saved)).thenReturn(savedResponse);
    when(repository.findById(assignmentId)).thenReturn(Optional.of(entity));

    assertEquals(savedResponse, service.create(courseId, request));
    assertEquals(savedResponse, service.update(courseId, assignmentId, request));
    service.delete(courseId, assignmentId);
    verify(repository).deleteById(assignmentId);
  }

  @Test
  void shouldRejectUnknownOrWrongCourseAssignment() {
    UUID courseId = UUID.randomUUID();
    UUID otherCourseId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();
    when(restMapper.toDomain(any(CourseAssignmentRequest.class)))
        .thenReturn(new CourseAssignment(null, null, UUID.randomUUID(), UUID.randomUUID()));
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

  private CourseAssignmentRequest validAssignment() {
    return new CourseAssignmentRequest(UUID.randomUUID(), UUID.randomUUID());
  }

  @Test
  void shouldRejectUnassignedTeacherFromReadingAssignments() {
    UUID courseId = UUID.randomUUID();
    var teacher = new User();
    teacher.setId(UUID.randomUUID());
    teacher.setUserRole(UserRole.TEACHER);
    when(currentUserService.getCurrentUser()).thenReturn(teacher);
    when(repository.existsByCourse_IdAndTeacherId(courseId, teacher.getId())).thenReturn(false);

    assertThrows(ForbiddenException.class, () -> service.listForCourse(courseId));
  }
}
