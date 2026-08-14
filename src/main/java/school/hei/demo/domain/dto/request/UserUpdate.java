package school.hei.demo.domain.dto.request;

import java.util.UUID;
import school.hei.demo.enums.UserRole;

public record UserUpdate(
    String reference,
    String firstName,
    String lastName,
    String email,
    String password,
    UserRole userRole,
    UUID specialtyId,
    Integer entryYear) {}
