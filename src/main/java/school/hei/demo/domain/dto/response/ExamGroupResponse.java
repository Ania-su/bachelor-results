package school.hei.demo.domain.dto.response;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ExamGroupResponse {
  private UUID id;
  private UUID examId;
  private UUID groupId;
}
