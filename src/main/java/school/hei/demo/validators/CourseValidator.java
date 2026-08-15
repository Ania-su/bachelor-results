package school.hei.demo.validators;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.Course;
import school.hei.demo.exception.BadRequestException;

@Component
public class CourseValidator {

  public void validate(Course course) {
    if (course == null) {
      throw new BadRequestException("Request body is required");
    }
    if (isBlank(course.getReference())) {
      throw new BadRequestException("reference is required");
    }
    if (course.getReference().length() > 8) {
      throw new BadRequestException("reference must be at most 8 characters");
    }
    if (isBlank(course.getTitle())) {
      throw new BadRequestException("title is required");
    }
    if (course.getSemester() == null || course.getSemester() < 1 || course.getSemester() > 6) {
      throw new BadRequestException("semester must be between 1 and 6");
    }
    if (course.getCredits() == null || course.getCredits() <= 0) {
      throw new BadRequestException("credits must be greater than 0");
    }
  }

  private boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
