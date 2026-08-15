package school.hei.demo.validators;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.CourseAssignment;

@Component
public class CourseAssignmentValidator {

  public void validate(CourseAssignment assignment) {
    if (assignment.getCourseId() == null) {
      throw new IllegalArgumentException("courseId is required");
    }
    if (assignment.getTeacherId() == null) {
      throw new IllegalArgumentException("teacherId is required");
    }
    if (assignment.getGroupId() == null) {
      throw new IllegalArgumentException("groupId is required");
    }
  }
}
