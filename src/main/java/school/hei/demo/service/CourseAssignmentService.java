package school.hei.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.CourseAssignmentRequest;
import school.hei.demo.domain.dto.response.CourseAssignmentResponse;
import school.hei.demo.domain.mappers.CourseAssignmentMapper;
import school.hei.demo.endpoint.rest.controller.mapper.CourseAssignmentRestMapper;
import school.hei.demo.entity.CourseAssignment;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.ForbiddenException;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseAssignmentRepository;
import school.hei.demo.validators.CourseAssignmentValidator;

@Service
@AllArgsConstructor
public class CourseAssignmentService {

  private final CourseAssignmentRepository repository;
  private final CourseAssignmentMapper mapper;
  private final CourseAssignmentValidator validator;
  private final CurrentUserService currentUserService;
  private final CourseAssignmentRestMapper restMapper;

  public List<CourseAssignmentResponse> listForCourse(UUID courseId) {
    ensureTeacherAssignedToCourse(courseId);
    return repository.findAllByCourse_Id(courseId).stream()
        .map(mapper::toDomain)
        .map(restMapper::toResponse)
        .toList();
  }

  public CourseAssignmentResponse get(UUID courseId, UUID assignmentId) {
    ensureTeacherAssignedToCourse(courseId);
    var assignment =
        repository
            .findById(assignmentId)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Assignment " + assignmentId + " not found"));
    ensureBelongsToCourse(assignment, courseId);
    return restMapper.toResponse(assignment);
  }

  public CourseAssignmentResponse create(UUID courseId, CourseAssignmentRequest request) {
    var assignment = restMapper.toDomain(request);
    assignment.setCourseId(courseId);
    validator.validate(assignment);
    return restMapper.toResponse(mapper.toDomain(repository.save(mapper.toEntity(assignment))));
  }

  public CourseAssignmentResponse update(
      UUID courseId, UUID assignmentId, CourseAssignmentRequest request) {
    var updated = restMapper.toDomain(request);
    updated.setCourseId(courseId);
    validator.validate(updated);
    var existing =
        repository
            .findById(assignmentId)
            .orElseThrow(() -> new NotFoundException("Assignment " + assignmentId + " not found"));
    ensureBelongsToCourse(mapper.toDomain(existing), courseId);

    var toSave = mapper.toEntity(updated.toBuilder().id(assignmentId).build());
    return restMapper.toResponse(mapper.toDomain(repository.save(toSave)));
  }

  public void delete(UUID courseId, UUID assignmentId) {
    var assignment = get(courseId, assignmentId);
    repository.deleteById(assignment.getId());
  }

  private void ensureBelongsToCourse(CourseAssignment assignment, UUID courseId) {
    if (!assignment.getCourseId().equals(courseId)) {
      throw new NotFoundException("Assignment not found for this course");
    }
  }

  private void ensureTeacherAssignedToCourse(UUID courseId) {
    var currentUser = currentUserService.getCurrentUser();
    if (currentUser.getUserRole() == UserRole.TEACHER
        && !isUserAssignedToCourse(currentUser.getId(), courseId)) {
      throw new ForbiddenException("Teacher is not assigned to this course");
    }
  }

  private boolean isUserAssignedToCourse(UUID userId, UUID courseId) {
    return repository.existsByCourse_IdAndTeacherId(courseId, userId);
  }
}
