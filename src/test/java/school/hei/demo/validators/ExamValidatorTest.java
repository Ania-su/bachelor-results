package school.hei.demo.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.demo.entity.Exam;

class ExamValidatorTest {
  private final ExamValidator validator = new ExamValidator();

  @Test
  void shouldAcceptValidExam() {
    Exam exam = new Exam(UUID.randomUUID(), UUID.randomUUID(), Instant.now(), BigDecimal.ONE);
    assertDoesNotThrow(() -> validator.validate(exam));
  }

  @Test
  void shouldRejectMissingAndInvalidFields() {
    UUID courseId = UUID.randomUUID();
    Instant date = Instant.now();
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(new Exam(null, null, date, BigDecimal.ONE)));
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(new Exam(null, courseId, null, BigDecimal.ONE)));
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(new Exam(null, courseId, date, null)));
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(new Exam(null, courseId, date, BigDecimal.ZERO)));
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validate(new Exam(null, courseId, date, BigDecimal.valueOf(1.01))));
  }
}
