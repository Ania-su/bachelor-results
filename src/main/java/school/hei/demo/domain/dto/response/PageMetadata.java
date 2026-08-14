package school.hei.demo.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageMetadata {
  private Integer page;
  private Integer pageSize;
  private Integer totalElements;
  private Integer totalPages;
}
