package school.hei.demo.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.entity.Specialty;

@Component
public class SpecialtyRestMapper {

  public SpecialtyResponse toResponse(Specialty specialty) {
    if (specialty == null) return null;
    return SpecialtyResponse.builder()
        .id(specialty.getId())
        .codeType(specialty.getCode())
        .label(specialty.getLabel())
        .build();
  }

  public Specialty toDomain(SpecialtyRequest request) {
    if (request == null) return null;
    return Specialty.builder().code(request.getCodeType()).label(request.getLabel()).build();
  }
}
