package school.hei.demo.domain.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PromotionDownloadResponse {
  private String downloadUrl;
}
