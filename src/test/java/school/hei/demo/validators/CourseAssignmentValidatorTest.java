package school.hei.demo.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.demo.entity.CourseAssignment;

class CourseAssignmentValidatorTest {
  private final CourseAssignmentValidator validator = new CourseAssignmentValidator();

  @Test
  void shouldAcceptValidAssignment() {
    CourseAssignment assignment =
        new CourseAssignment(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    assertDoesNotThrow(() -> validator.validate(assignment));
  }

  @Test
  void shouldRejectMissingFields() {
    UUID teacherId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(new CourseAssignment(null, null, teacherId, groupId)));
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(new CourseAssignment(null, UUID.randomUUID(), null, groupId)));
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(new CourseAssignment(null, UUID.randomUUID(), teacherId, null)));
  }
}
