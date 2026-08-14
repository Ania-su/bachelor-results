package school.hei.demo.endpoint.rest.controller.validator;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.Course;

@Component
public class CourseValidator {

  public void validate(Course course) {
    if (isBlank(course.getReference())) {
      throw new IllegalArgumentException("reference is required");
    }
    if (course.getReference().length() > 8) {
      throw new IllegalArgumentException("reference must be at most 8 characters");
    }
    if (isBlank(course.getTitle())) {
      throw new IllegalArgumentException("title is required");
    }
    if (course.getSemester() == null || course.getSemester() < 1 || course.getSemester() > 6) {
      throw new IllegalArgumentException("semester must be between 1 and 6");
    }
    if (course.getCredits() == null || course.getCredits() <= 0) {
      throw new IllegalArgumentException("credits must be greater than 0");
    }
  }

  private boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
