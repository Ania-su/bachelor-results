package school.hei.demo.domain.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamGradeResponse {
  private UUID id;
  private UUID courseId;
  private Instant dateExam;
  private Double coef;
  private Double grade;
}
