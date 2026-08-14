package school.hei.demo.domain.dto.request;

import lombok.*;
import school.hei.demo.enums.CodeType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SpecialtyRequest {
  private CodeType codeType;
  private String label;
}
