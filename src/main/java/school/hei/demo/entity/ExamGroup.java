package school.hei.demo.entity;

import lombok.*;
import java.util.UUID;

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
