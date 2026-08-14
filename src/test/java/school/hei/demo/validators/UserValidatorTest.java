package school.hei.demo.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.request.UserUpdate;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.BadRequestException;

class UserValidatorTest {
  private UserValidator validator;

  @BeforeEach
  void setUp() {
    validator = new UserValidator();
  }

  @Test
  void shouldAcceptCompleteRequest() {
    assertDoesNotThrow(() -> validator.validate(validRequest()));
  }

  @Test
  void shouldRejectNullRequest() {
    assertThrows(BadRequestException.class, () -> validator.validate(null));
  }

  @Test
  void shouldRejectMissingTextFields() {
    String[] fields = {"reference", "firstName", "lastName", "email", "password"};
    for (String field : fields) {
      UserCreate nullRequest = validRequestWith(field, null);
      assertThrows(BadRequestException.class, () -> validator.validate(nullRequest));
      UserCreate blankRequest = validRequestWith(field, " ");
      assertThrows(BadRequestException.class, () -> validator.validate(blankRequest));
    }
  }

  @Test
  void shouldRejectMissingNonTextFields() {
    assertThrows(BadRequestException.class, () -> validator.validate(requestWithNullRole()));
    assertThrows(BadRequestException.class, () -> validator.validate(requestWithNullSpecialty()));
    assertThrows(BadRequestException.class, () -> validator.validate(requestWithNullEntryYear()));
  }

  @Test
  void shouldAcceptValidUuid() {
    assertDoesNotThrow(() -> validator.validateUuid(UUID.randomUUID().toString()));
  }

  @Test
  void shouldRejectInvalidUuid() {
    assertThrows(BadRequestException.class, () -> validator.validateUuid(null));
    assertThrows(BadRequestException.class, () -> validator.validateUuid(" "));
    assertThrows(BadRequestException.class, () -> validator.validateUuid("not-a-uuid"));
  }

  @Test
  void shouldAcceptPartialUpdate() {
    assertDoesNotThrow(() -> validator.validateUpdate(new UserUpdate(null, "John", null, null, null, null, null, null)));
    assertDoesNotThrow(() -> validator.validateUpdate(new UserUpdate(null, null, null, null, null, null, null, null)));
  }

  @Test
  void shouldRejectInvalidPartialUpdate() {
    assertThrows(BadRequestException.class, () -> validator.validateUpdate(null));
    assertThrows(BadRequestException.class, () -> validator.validateUpdate(new UserUpdate(" ", null, null, null, null, null, null, null)));
  }

  private UserCreate validRequest() {
    return new UserCreate(
        "REF001", "John", "Doe", "john@example.com", "password", UserRole.STUDENT,
        UUID.randomUUID(), 2025);
  }

  private UserCreate validRequestWith(String field, String value) {
    UserCreate request = validRequest();
    return switch (field) {
      case "reference" -> new UserCreate(value, request.firstName(), request.lastName(), request.email(), request.password(), request.userRole(), request.specialtyId(), request.entryYear());
      case "firstName" -> new UserCreate(request.reference(), value, request.lastName(), request.email(), request.password(), request.userRole(), request.specialtyId(), request.entryYear());
      case "lastName" -> new UserCreate(request.reference(), request.firstName(), value, request.email(), request.password(), request.userRole(), request.specialtyId(), request.entryYear());
      case "email" -> new UserCreate(request.reference(), request.firstName(), request.lastName(), value, request.password(), request.userRole(), request.specialtyId(), request.entryYear());
      case "password" -> new UserCreate(request.reference(), request.firstName(), request.lastName(), request.email(), value, request.userRole(), request.specialtyId(), request.entryYear());
      default -> throw new IllegalArgumentException(field);
    };
  }

  private UserCreate requestWithNullRole() {
    UserCreate request = validRequest();
    return new UserCreate(request.reference(), request.firstName(), request.lastName(), request.email(), request.password(), null, request.specialtyId(), request.entryYear());
  }

  private UserCreate requestWithNullSpecialty() {
    UserCreate request = validRequest();
    return new UserCreate(request.reference(), request.firstName(), request.lastName(), request.email(), request.password(), request.userRole(), null, request.entryYear());
  }

  private UserCreate requestWithNullEntryYear() {
    UserCreate request = validRequest();
    return new UserCreate(request.reference(), request.firstName(), request.lastName(), request.email(), request.password(), request.userRole(), request.specialtyId(), null);
  }
}
