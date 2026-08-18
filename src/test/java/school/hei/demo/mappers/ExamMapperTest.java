package school.hei.demo.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.mappers.ExamMapper;
import school.hei.demo.entity.Exam;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JExam;

@ExtendWith(MockitoExtension.class)
class ExamMapperTest {
  @Mock CourseRepository courseRepository;

  @Test
  void shouldMapBothDirections() {
    UUID id = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Instant date = Instant.parse("2025-06-15T10:00:00Z");
    BigDecimal coef = BigDecimal.valueOf(0.5);
    JCourse course = JCourse.builder().id(courseId).build();
    JExam entity = JExam.builder().id(id).course(course).dateExam(date).coef(coef).build();
    ExamMapper mapper = new ExamMapper(courseRepository);
    Exam domain = mapper.toDomain(entity);
    when(courseRepository.getReferenceById(courseId)).thenReturn(course);

    assertEquals(new Exam(id, courseId, date, coef), domain);
    assertEquals(entity, mapper.toEntity(domain));
  }

  @Test
  void shouldReturnNullForNull() {
    ExamMapper mapper = new ExamMapper(courseRepository);
    assertNull(mapper.toDomain((JExam) null));
    assertNull(mapper.toEntity(null));
  }
}
