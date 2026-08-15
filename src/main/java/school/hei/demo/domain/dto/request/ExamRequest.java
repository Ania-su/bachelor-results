package school.hei.demo.domain.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExamRequest {
  private Instant dateExam;
  private BigDecimal coef;
}
