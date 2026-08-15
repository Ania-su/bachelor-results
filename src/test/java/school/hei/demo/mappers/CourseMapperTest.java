package school.hei.demo.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.demo.domain.mappers.CourseMapper;
import school.hei.demo.entity.Course;
import school.hei.demo.repository.model.JCourse;

class CourseMapperTest {
  private final CourseMapper mapper = new CourseMapper();

  @Test
  void shouldMapBothDirections() {
    UUID id = UUID.randomUUID();
    JCourse entity = new JCourse(id, "CS", "Algorithms", 2, 6);
    Course domain = mapper.toDomain(entity);
    assertEquals(id, domain.getId());
    assertEquals("Algorithms", domain.getTitle());
    assertEquals(entity, mapper.toEntity(domain));
  }

  @Test
  void shouldReturnNullForNull() {
    assertNull(mapper.toDomain(null));
    assertNull(mapper.toEntity(null));
  }
}
