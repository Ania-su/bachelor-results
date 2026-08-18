package school.hei.demo.domain.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.CourseAssignmentRequest;
import school.hei.demo.domain.dto.response.CourseAssignmentResponse;
import school.hei.demo.entity.CourseAssignment;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.repository.model.JCourseAssignment;

@Component
@AllArgsConstructor
public class CourseAssignmentMapper {

  private final CourseRepository courseRepository;
  private final GroupRepository groupRepository;

  public CourseAssignmentResponse toResponse(CourseAssignment a) {
    if (a == null) return null;
    return CourseAssignmentResponse.builder()
        .id(a.getId())
        .courseId(a.getCourseId())
        .teacherId(a.getTeacherId())
        .groupId(a.getGroupId())
        .build();
  }

  public CourseAssignment toDomain(CourseAssignmentRequest request) {
    return CourseAssignment.builder()
        .teacherId(request.getTeacherId())
        .groupId(request.getGroupId())
        .build();
  }

  public CourseAssignment toDomain(JCourseAssignment j) {
    if (j == null) return null;
    return CourseAssignment.builder()
        .id(j.getId())
        .courseId(j.getCourse().getId())
        .teacherId(j.getTeacherId())
        .groupId(j.getGroup().getId())
        .build();
  }

  public JCourseAssignment toEntity(CourseAssignment a) {
    if (a == null) return null;
    return JCourseAssignment.builder()
        .id(a.getId())
        .course(courseRepository.getReferenceById(a.getCourseId()))
        .teacherId(a.getTeacherId())
        .group(groupRepository.getReferenceById(a.getGroupId()))
        .build();
  }
}
