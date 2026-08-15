package school.hei.demo.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import school.hei.demo.entity.Group;
import school.hei.demo.exception.BadRequestException;

class GroupValidatorTest {
  private final GroupValidator validator = new GroupValidator();

  @Test
  void shouldAcceptValidGroup() {
    assertDoesNotThrow(() -> validator.validate(new Group(null, "A1", 2025)));
  }

  @Test
  void shouldRejectInvalidGroup() {
    assertThrows(BadRequestException.class, () -> validator.validate(null));
    assertThrows(BadRequestException.class, () -> validator.validate(new Group(null, " ", 2025)));
    assertThrows(BadRequestException.class, () -> validator.validate(new Group(null, "ABC", 2025)));
    assertThrows(BadRequestException.class, () -> validator.validate(new Group(null, "A1", null)));
  }
}
