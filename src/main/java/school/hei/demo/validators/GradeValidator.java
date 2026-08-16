package school.hei.demo.validators;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.GradeCreate;
import school.hei.demo.domain.dto.request.GradeUpdate;
import school.hei.demo.exception.BadRequestException;

@Component
public class GradeValidator {
  public void validateCreate(GradeCreate request) {
    if (request == null) invalid("Request body is required");
    if (request.getStudentId() == null) invalid("studentId is required");
    validateValue(request.getValue());
  }

  public void validateUpdate(GradeUpdate request) {
    if (request == null) invalid("Request body is required");
    validateValue(request.getValue());
    validateReason(request.getReason());
  }

  private void validateValue(BigDecimal value) {
    if (value == null) invalid("value is required");
    if (value.scale() > 2
        || value.compareTo(BigDecimal.ZERO) < 0
        || value.compareTo(BigDecimal.valueOf(20)) > 0) {
      invalid("value must be between 0 and 20 with at most 2 decimals");
    }
  }

  private void validateReason(String reason) {
    if (reason == null || reason.isBlank()) {
      invalid("reason must contain at least one character");
    }
  }

  private void invalid(String message) {
    throw new BadRequestException(message);
  }
}
