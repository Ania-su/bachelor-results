package school.hei.demo.validators;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import school.hei.demo.domain.dto.request.UserCreate;

@Component
public class UserValidator {
  public void validate(UserCreate request) {
    requireText(request.reference(), "reference");
    requireText(request.firstName(), "firstName");
    requireText(request.lastName(), "lastName");
    requireText(request.email(), "email");
    requireText(request.password(), "password");

    if (request.userRole() == null) {
      invalid("userRole is required");
    }
    if (request.specialtyCodeType() == null) {
      invalid("specialtyCodeType is required");
    }
    if (request.entryYear() == null) {
      invalid("entryYear is required");
    }
  }

  private void requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      invalid(fieldName + " is required");
    }
  }

  private void invalid(String message) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }
}
