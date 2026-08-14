package school.hei.demo.entity;

import java.util.UUID;
import lombok.*;
import school.hei.demo.enums.CodeType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Specialty {
  @EqualsAndHashCode.Include private UUID id;

  private CodeType code;
  private String label;
}
