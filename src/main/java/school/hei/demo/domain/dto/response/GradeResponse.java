package school.hei.demo.domain.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeResponse {
  private UUID id;
  private UUID studentId;
  private UUID examId;
  private BigDecimal value;
  private Instant updatedAt;
}
