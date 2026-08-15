package school.hei.demo.domain.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExamResponse {
  private UUID id;
  private UUID courseId;
  private Instant dateExam;
  private BigDecimal coef;
}
