package school.hei.demo.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.demo.domain.dto.request.GradeCreate;
import school.hei.demo.domain.dto.request.GradeUpdate;
import school.hei.demo.exception.BadRequestException;

class GradeValidatorTest {
  private final GradeValidator validator = new GradeValidator();

  @Test
  void shouldAcceptValidGradeRequests() {
    var studentId = UUID.randomUUID();
    assertDoesNotThrow(
        () -> validator.validateCreate(new GradeCreate(studentId, BigDecimal.valueOf(12.5))));
    assertDoesNotThrow(
        () -> validator.validateUpdate(new GradeUpdate(BigDecimal.valueOf(13), "Correction")));
  }

  @Test
  void shouldRejectInvalidCreateValues() {
    var studentId = UUID.randomUUID();
    assertThrows(BadRequestException.class, () -> validator.validateCreate(null));
    assertThrows(
        BadRequestException.class,
        () -> validator.validateCreate(new GradeCreate(null, BigDecimal.TEN)));
    assertThrows(
        BadRequestException.class,
        () -> validator.validateCreate(new GradeCreate(studentId, null)));
    assertThrows(
        BadRequestException.class,
        () -> validator.validateCreate(new GradeCreate(studentId, BigDecimal.valueOf(20.001))));
    assertThrows(
        BadRequestException.class,
        () -> validator.validateCreate(new GradeCreate(studentId, BigDecimal.valueOf(-1))));
    assertThrows(
        BadRequestException.class,
        () -> validator.validateCreate(new GradeCreate(studentId, BigDecimal.valueOf(21))));
  }

  @Test
  void shouldRejectInvalidUpdateRequests() {
    assertThrows(BadRequestException.class, () -> validator.validateUpdate(null));
    assertThrows(
        BadRequestException.class,
        () -> validator.validateUpdate(new GradeUpdate(null, "Correction")));
    assertThrows(
        BadRequestException.class,
        () -> validator.validateUpdate(new GradeUpdate(BigDecimal.TEN, " ")));
  }
}
