package school.hei.demo.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.demo.domain.mappers.SpecialtyMapper;
import school.hei.demo.entity.Specialty;
import school.hei.demo.enums.CodeType;
import school.hei.demo.repository.model.JSpecialty;

class SpecialtyMapperTest {
  private final SpecialtyMapper mapper = new SpecialtyMapper();

  @Test
  void shouldMapBothDirections() {
    UUID id = UUID.randomUUID();
    JSpecialty entity = new JSpecialty(id, CodeType.EL, "Software");
    Specialty domain = mapper.toDomain(entity);
    assertEquals(CodeType.EL, domain.getCode());
    assertEquals("Software", domain.getLabel());
    assertEquals(entity, mapper.toEntity(domain));
  }

  @Test
  void shouldReturnNullForNull() {
    assertNull(mapper.toDomain((JSpecialty) null));
    assertNull(mapper.toEntity(null));
  }
}
