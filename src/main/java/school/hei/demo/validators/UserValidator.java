package school.hei.demo.validators;

import java.util.UUID;
import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.request.UserUpdate;
import school.hei.demo.exception.BadRequestException;

@Component
public class UserValidator {
  public UUID validateUuid(String value) {
    if (value == null || value.isBlank()) {
      throw new BadRequestException("User id is required");
    }

    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("User id must be a valid UUID");
    }
  }

  public void validatePagination(int page, int pageSize) {
    if (page < 0) {
      throw new BadRequestException("page must be greater than or equal to 0");
    }
    if (pageSize < 1 || pageSize > 100) {
      throw new BadRequestException("pageSize must be between 1 and 100");
    }
  }

  public void validate(UserCreate request) {
    if (request == null) {
      invalid("Request body is required");
    }

    requireText(request.reference(), "reference");
    requireText(request.firstName(), "firstName");
    requireText(request.lastName(), "lastName");
    requireText(request.email(), "email");
    requireText(request.password(), "password");

    if (request.userRole() == null) {
      invalid("userRole is required");
    }
    if (request.specialtyId() == null) {
      invalid("specialtyId is required");
    }
    if (request.entryYear() == null) {
      invalid("entryYear is required");
    }
  }

  public void validateUpdate(UserUpdate request) {
    if (request == null) {
      invalid("Request body is required");
    }

    validateOptionalText(request.reference(), "reference");
    validateOptionalText(request.firstName(), "firstName");
    validateOptionalText(request.lastName(), "lastName");
    validateOptionalText(request.email(), "email");
    validateOptionalText(request.password(), "password");
  }

  private void requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      invalid(fieldName + " is required");
    }
  }

  private void validateOptionalText(String value, String fieldName) {
    if (value != null && value.isBlank()) {
      invalid(fieldName + " cannot be blank");
    }
  }

  private void invalid(String message) {
    throw new BadRequestException(message);
  }
}
