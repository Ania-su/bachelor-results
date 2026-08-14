package school.hei.demo.domain.mappers;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.Specialty;
import school.hei.demo.repository.model.JSpecialty;

@Component
public class SpecialtyMapper {

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
