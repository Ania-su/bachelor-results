package school.hei.demo.domain.dto.request;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CourseAssignmentRequest {
    private UUID teacherId;
    private UUID groupId;
}
