package school.hei.demo.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.demo.domain.mappers.GroupMapper;
import school.hei.demo.entity.Group;
import school.hei.demo.repository.model.JGroup;

class GroupMapperTest {
  private final GroupMapper mapper = new GroupMapper();

  @Test
  void shouldMapBothDirections() {
    UUID id = UUID.randomUUID();
    JGroup entity = new JGroup(id, "A1", 2025);
    Group domain = mapper.toDomain(entity);
    assertEquals(id, domain.getId());
    assertEquals("A1", domain.getReference());
    assertEquals(entity, mapper.toEntity(domain));
  }

  @Test
  void shouldReturnNullForNull() {
    assertNull(mapper.toDomain((JGroup) null));
    assertNull(mapper.toEntity(null));
  }
}
