package school.hei.demo.validators;

import org.springframework.stereotype.Component;
import school.hei.demo.exception.BadRequestException;

@Component
public class PaginationValidator {
  public void validate(int page, int pageSize) {
    if (page < 0) {
      throw new BadRequestException("page must be greater than or equal to 0");
    }
    if (pageSize < 1 || pageSize > 100) {
      throw new BadRequestException("pageSize must be between 1 and 100");
    }
  }
}
