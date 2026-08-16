package school.hei.demo.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "grade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JGrade {
  @Id @GeneratedValue @EqualsAndHashCode.Include private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_student", nullable = false)
  private JUser student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_exam", nullable = false)
  private JExam exam;

  @Column(name = "value", nullable = false, precision = 4, scale = 2)
  private BigDecimal value;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
