package school.hei.demo.validators;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.Specialty;
import school.hei.demo.exception.BadRequestException;

@Component
public class SpecialtyValidator {

  public void validate(Specialty specialty) {
    if (specialty == null) {
      throw new BadRequestException("Request body is required");
    }
    if (specialty.getCode() == null) {
      throw new BadRequestException("codeType is required");
    }
    if (specialty.getLabel() == null || specialty.getLabel().isBlank()) {
      throw new BadRequestException("label is required");
    }
    if (specialty.getLabel().length() > 255) {
      throw new BadRequestException("label must be at most 255 characters");
    }
  }
}
