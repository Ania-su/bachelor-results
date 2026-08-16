package school.hei.demo.domain.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradePage {
  private List<GradeResponse> content;
  private PageMetadata page;
}
