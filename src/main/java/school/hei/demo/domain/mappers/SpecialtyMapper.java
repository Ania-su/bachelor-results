package school.hei.demo.domain.mappers;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.entity.Specialty;
import school.hei.demo.repository.model.JSpecialty;

@Component
public class SpecialtyMapper {

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

  public Specialty toDomain(JSpecialty jSpecialty) {
    if (jSpecialty == null) return null;
    return Specialty.builder()
        .id(jSpecialty.getId())
        .code(jSpecialty.getCode())
        .label(jSpecialty.getLabel())
        .build();
  }

  public JSpecialty toEntity(Specialty specialty) {
    if (specialty == null) return null;
    return JSpecialty.builder()
        .id(specialty.getId())
        .code(specialty.getCode())
        .label(specialty.getLabel())
        .build();
  }
}
