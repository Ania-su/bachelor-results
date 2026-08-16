package school.hei.demo.repository.model;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import school.hei.demo.enums.UserRole;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class JUser {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(nullable = false, unique = true, length = 8)
  private String reference;

  @Column(name = "first_name", nullable = false, length = 255)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 255)
  private String lastName;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String password;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "user_role", nullable = false, columnDefinition = "user_role")
  private UserRole userRole;

  @Column(name = "id_specialty", nullable = false)
  private UUID specialtyId;

  @Column(name = "entry_year")
  private Integer entryYear;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  private void setCreatedAt() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
