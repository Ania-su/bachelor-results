package school.hei.demo.validators;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.Group;
import school.hei.demo.exception.BadRequestException;

@Component
public class GroupValidator {

  public void validate(Group group) {
    if (group == null) {
      throw new BadRequestException("Request body is required");
    }
    if (group.getReference() == null || group.getReference().isBlank()) {
      throw new BadRequestException("reference is required");
    }
    if (group.getReference().length() > 2) {
      throw new BadRequestException("reference must be at most 2 characters");
    }
    if (group.getAcademicYear() == null) {
      throw new BadRequestException("academicYear is required");
    }
  }
}
