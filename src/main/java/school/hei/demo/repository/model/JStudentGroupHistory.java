package school.hei.demo.repository.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "student_group_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JStudentGroupHistory {

  @Id @GeneratedValue @EqualsAndHashCode.Include private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_student", nullable = false)
  private JUser student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_group", nullable = false)
  private JGroup group;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;
}
