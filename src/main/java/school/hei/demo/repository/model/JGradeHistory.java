package school.hei.demo.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "grade_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JGradeHistory {
  @Id @GeneratedValue private UUID id;

  @Column(name = "id_note", nullable = false)
  private UUID gradeId;

  @Column(name = "old_value", precision = 4, scale = 2)
  private BigDecimal oldValue;

  @Column(name = "new_value", precision = 4, scale = 2)
  private BigDecimal newValue;

  @Column(nullable = false, length = 500)
  private String reason;

  @Column(name = "changed_by")
  private UUID changedBy;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;
}
