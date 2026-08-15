package school.hei.demo.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import school.hei.demo.entity.Course;
import school.hei.demo.exception.BadRequestException;

class CourseValidatorTest {
  private final CourseValidator validator = new CourseValidator();

  @Test
  void shouldAcceptValidCourse() {
    assertDoesNotThrow(() -> validator.validate(new Course(null, "CS", "Algorithms", 2, 6)));
  }

  @Test
  void shouldRejectMissingAndOutOfRangeFields() {
    assertThrows(BadRequestException.class, () -> validator.validate(null));
    assertThrows(
        BadRequestException.class, () -> validator.validate(new Course(null, " ", "Title", 1, 1)));
    assertThrows(
        BadRequestException.class,
        () -> validator.validate(new Course(null, "123456789", "Title", 1, 1)));
    assertThrows(
        BadRequestException.class, () -> validator.validate(new Course(null, "CS", " ", 1, 1)));
    assertThrows(
        BadRequestException.class, () -> validator.validate(new Course(null, "CS", "Title", 0, 1)));
    assertThrows(
        BadRequestException.class, () -> validator.validate(new Course(null, "CS", "Title", 7, 1)));
    assertThrows(
        BadRequestException.class, () -> validator.validate(new Course(null, "CS", "Title", 1, 0)));
  }
}
