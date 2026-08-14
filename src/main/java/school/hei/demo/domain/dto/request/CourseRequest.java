package school.hei.demo.domain.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CourseRequest {
  private String reference;
  private String title;
  private Integer semester;
  private Integer credits;
}
