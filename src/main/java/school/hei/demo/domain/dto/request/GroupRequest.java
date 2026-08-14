package school.hei.demo.domain.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GroupRequest {
    private String reference;
    private Integer academicYear;
}
