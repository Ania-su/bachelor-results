package school.hei.demo.endpoint.rest.model;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CourseResponse {
  private UUID id;
  private String reference;
  private String title;
  private Integer semester;
  private Integer credits;
}
