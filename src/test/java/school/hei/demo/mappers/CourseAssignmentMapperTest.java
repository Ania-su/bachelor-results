package school.hei.demo.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.mappers.CourseAssignmentMapper;
import school.hei.demo.entity.CourseAssignment;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JCourseAssignment;
import school.hei.demo.repository.model.JGroup;

@ExtendWith(MockitoExtension.class)
class CourseAssignmentMapperTest {
  @Mock CourseRepository courseRepository;
  @Mock GroupRepository groupRepository;

  @Test
  void shouldMapBothDirections() {
    UUID id = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    JCourse course = JCourse.builder().id(courseId).build();
    JGroup group = JGroup.builder().id(groupId).build();
    JCourseAssignment entity =
        JCourseAssignment.builder().id(id).course(course).teacherId(teacherId).group(group).build();
    CourseAssignmentMapper mapper = new CourseAssignmentMapper(courseRepository, groupRepository);
    CourseAssignment domain = mapper.toDomain(entity);
    when(courseRepository.getReferenceById(courseId)).thenReturn(course);
    when(groupRepository.getReferenceById(groupId)).thenReturn(group);

    assertEquals(new CourseAssignment(id, courseId, teacherId, groupId), domain);
    assertEquals(entity, mapper.toEntity(domain));
  }

  @Test
  void shouldReturnNullForNull() {
    CourseAssignmentMapper mapper = new CourseAssignmentMapper(courseRepository, groupRepository);
    assertNull(mapper.toDomain((JCourseAssignment) null));
    assertNull(mapper.toEntity(null));
  }
}
