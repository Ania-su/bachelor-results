package school.hei.demo.domain.dto.request;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeCreate {
  private UUID studentId;
  private BigDecimal value;
}
