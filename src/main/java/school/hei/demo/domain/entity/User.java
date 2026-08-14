package school.hei.demo.domain.entity;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.demo.enums.CodeType;
import school.hei.demo.enums.UserRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private UUID id;
  private String reference;
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private UserRole userRole;
  private CodeType specialtyCodeType;
  private Integer entryYear;
  private Instant createdAt;
}
