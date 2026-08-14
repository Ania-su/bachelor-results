package school.hei.demo.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.demo.enums.CodeType;
import school.hei.demo.enums.UserRole;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class JUser {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 8)
  private String reference;

  @Column(nullable = false, length = 255)
  private String firstName;

  @Column(nullable = false, length = 255)
  private String lastName;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole userRole;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CodeType specialtyCodeType;

  @Column(nullable = false)
  private Integer entryYear;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  private void setCreatedAt() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
