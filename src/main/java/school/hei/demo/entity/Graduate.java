package school.hei.demo.entity;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Graduate {
  private int rank;
  private String reference;
  private String firstName;
  private String lastName;
  private BigDecimal average;
}
