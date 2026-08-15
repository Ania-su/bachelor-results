package school.hei.demo.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "exam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JExam {

  @Id @GeneratedValue @EqualsAndHashCode.Include private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_course", nullable = false)
  private JCourse course;

  @Column(name = "date_exam", nullable = false)
  private Instant dateExam;

  @Column(name = "coef", nullable = false, precision = 3, scale = 2)
  private BigDecimal coef;
}
