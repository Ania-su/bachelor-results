package school.hei.demo.domain.dto.response;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GroupResponse {
  private UUID id;
  private String reference;
  private Integer academicYear;
}
