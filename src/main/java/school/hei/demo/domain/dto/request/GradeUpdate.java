package school.hei.demo.domain.dto.request;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeUpdate {
  private BigDecimal value;
  private String reason;
}
