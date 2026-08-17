package school.hei.demo.entity;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseAverageResult {
  private final UUID courseId;
  private final Integer credits;
  private final BigDecimal average;
}
