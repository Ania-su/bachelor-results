package school.hei.demo.domain.dto.request;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CourseAssignmentRequest {
  private UUID teacherId;
  private UUID groupId;
}
