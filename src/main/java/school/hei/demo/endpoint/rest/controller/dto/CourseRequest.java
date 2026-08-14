package school.hei.demo.endpoint.rest.controller.dto;

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
