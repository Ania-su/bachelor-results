package school.hei.demo.domain.dto.response;

import java.util.UUID;
import lombok.*;
import school.hei.demo.enums.CodeType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SpecialtyResponse {
  private UUID id;
  private CodeType codeType;
  private String label;
}
