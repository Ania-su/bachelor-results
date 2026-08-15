package school.hei.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.mappers.CourseAssignmentMapper;
import school.hei.demo.entity.CourseAssignment;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseAssignmentRepository;
import school.hei.demo.validators.CourseAssignmentValidator;

@Service
@AllArgsConstructor
public class CourseAssignmentService {

  private final CourseAssignmentRepository repository;
  private final CourseAssignmentMapper mapper;
  private final CourseAssignmentValidator validator;

  public List<CourseAssignment> listForCourse(UUID courseId) {
    return repository.findAllByCourse_Id(courseId).stream().map(mapper::toDomain).toList();
  }

  public CourseAssignment get(UUID courseId, UUID assignmentId) {
    var assignment =
        repository
            .findById(assignmentId)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Assignment " + assignmentId + " not found"));
    ensureBelongsToCourse(assignment, courseId);
    return assignment;
  }

  public CourseAssignment create(UUID courseId, CourseAssignment assignment) {
    assignment.setCourseId(courseId);
    validator.validate(assignment);
    return mapper.toDomain(repository.save(mapper.toEntity(assignment)));
  }

  public CourseAssignment update(UUID courseId, UUID assignmentId, CourseAssignment updated) {
    updated.setCourseId(courseId);
    validator.validate(updated);
    var existing =
        repository
            .findById(assignmentId)
            .orElseThrow(() -> new NotFoundException("Assignment " + assignmentId + " not found"));
    ensureBelongsToCourse(mapper.toDomain(existing), courseId);

    var toSave = mapper.toEntity(updated.toBuilder().id(assignmentId).build());
    return mapper.toDomain(repository.save(toSave));
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
}
