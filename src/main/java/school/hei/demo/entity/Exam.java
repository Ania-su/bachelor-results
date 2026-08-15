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
public class Exam {

  @EqualsAndHashCode.Include private UUID id;

  private UUID courseId;
  private Instant dateExam;
  private BigDecimal coef;
}
