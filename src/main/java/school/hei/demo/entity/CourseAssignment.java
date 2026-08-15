package school.hei.demo.entity;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseAssignment {

  @EqualsAndHashCode.Include private UUID id;

  private UUID courseId;
  private UUID teacherId;
  private UUID groupId;
}
