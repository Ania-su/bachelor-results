package school.hei.demo.entity;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExamGroup {

  @EqualsAndHashCode.Include private UUID id;

  private UUID examId;
  private UUID groupId;
}
