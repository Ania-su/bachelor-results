package school.hei.demo.domain.dto.response;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CourseAssignmentResponse {
    private UUID id;
    private UUID courseId;
    private UUID teacherId;
    private UUID groupId;
}
