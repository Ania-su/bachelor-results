package school.hei.demo.entity;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Group {

  @EqualsAndHashCode.Include private UUID id;

  private String reference;
  private Integer academicYear;
}
