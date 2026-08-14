package school.hei.demo.validators;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.Specialty;

@Component
public class SpecialtyValidator {

  public void validate(Specialty specialty) {
    if (specialty.getCode() == null) {
      throw new IllegalArgumentException("codeType is required");
    }
    if (specialty.getLabel() != null && specialty.getLabel().length() > 255) {
      throw new IllegalArgumentException("label must be at most 255 characters");
    }
  }
}
