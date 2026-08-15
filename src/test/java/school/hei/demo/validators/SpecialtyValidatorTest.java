package school.hei.demo.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import school.hei.demo.entity.Specialty;
import school.hei.demo.enums.CodeType;
import school.hei.demo.exception.BadRequestException;

class SpecialtyValidatorTest {
  private final SpecialtyValidator validator = new SpecialtyValidator();

  @Test
  void shouldAcceptValidSpecialty() {
    assertDoesNotThrow(() -> validator.validate(new Specialty(null, CodeType.EL, "Software")));
  }

  @Test
  void shouldRejectMissingAndLongFields() {
    assertThrows(BadRequestException.class, () -> validator.validate(null));
    assertThrows(
        BadRequestException.class, () -> validator.validate(new Specialty(null, null, "Software")));
    assertThrows(
        BadRequestException.class, () -> validator.validate(new Specialty(null, CodeType.EL, " ")));
    assertThrows(
        BadRequestException.class,
        () -> validator.validate(new Specialty(null, CodeType.EL, "x".repeat(256))));
  }
}
