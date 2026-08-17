package school.hei.demo.entity;

import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StudentGroupHistory {

  @EqualsAndHashCode.Include private UUID id;

  private UUID studentId;
  private UUID groupId;
  private LocalDate startDate;
  private LocalDate endDate;
}
