package school.hei.demo.domain.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeHistoryResponse {
  private UUID id;
  private UUID gradeId;
  private BigDecimal oldValue;
  private BigDecimal newValue;
  private String reason;
  private UUID changedBy;
  private Instant changedAt;
}
