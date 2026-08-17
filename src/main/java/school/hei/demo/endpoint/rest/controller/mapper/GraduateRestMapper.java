package school.hei.demo.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.response.GraduateResponse;
import school.hei.demo.entity.Graduate;

@Component
public class GraduateRestMapper {

  public GraduateResponse toResponse(Graduate g) {
    return GraduateResponse.builder()
        .rank(g.getRank())
        .reference(g.getReference())
        .firstName(g.getFirstName())
        .lastName(g.getLastName())
        .average(g.getAverage())
        .build();
  }
}
