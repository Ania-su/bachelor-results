package school.hei.demo.domain.dto.response;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GraduateResponse {
  private int rank;
  private String reference;
  private String firstName;
  private String lastName;
  private BigDecimal average;
}
