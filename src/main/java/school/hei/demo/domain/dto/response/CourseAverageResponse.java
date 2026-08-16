package school.hei.demo.domain.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseAverageResponse {
  private UUID id;
  private String reference;
  private String title;
  private Integer semester;
  private Integer credits;
  private Double average;
}
