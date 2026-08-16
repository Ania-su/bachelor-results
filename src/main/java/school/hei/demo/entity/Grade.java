package school.hei.demo.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Grade {
  @EqualsAndHashCode.Include private UUID id;
  private UUID studentId;
  private UUID examId;
  private BigDecimal value;
  private Instant updatedAt;
}
