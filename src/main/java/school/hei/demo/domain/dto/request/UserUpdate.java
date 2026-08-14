package school.hei.demo.domain.dto.request;

import school.hei.demo.enums.CodeType;
import school.hei.demo.enums.UserRole;

public record UserUpdate(
    String reference,
    String firstName,
    String lastName,
    String email,
    String password,
    UserRole userRole,
    CodeType specialtyCodeType,
    Integer entryYear) {}
