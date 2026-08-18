package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.domain.dto.response.CoursePage;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.domain.mappers.CourseMapper;
import school.hei.demo.entity.Course;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.validators.CourseValidator;
import school.hei.demo.validators.PaginationValidator;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
  @Mock CourseRepository repository;
  @Mock CourseMapper mapper;
  @Mock CourseValidator validator;
  @Mock PaginationValidator paginationValidator;
  @InjectMocks CourseService service;

  @Test
  void shouldListWithSemesterSearchAndPagination() {
    JCourse entity = new JCourse(UUID.randomUUID(), "CS", "Algorithms", 2, 6);
    Course course = new Course(entity.getId(), "CS", "Algorithms", 2, 6);
    CourseResponse response = new CourseResponse(entity.getId(), "CS", "Algorithms", 2, 6);
    when(repository.findAllFiltered(eq(2), eq("algo"), any()))
        .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(1, 2), 3));
    when(mapper.toDomain(entity)).thenReturn(course);
    when(mapper.toResponse(course)).thenReturn(response);

    CoursePage page = service.list(2, "algo", 1, 2);
    assertEquals(1, page.getContent().size());
    assertEquals(3, page.getPage().getTotalElements());
    verify(paginationValidator).validate(1, 2);
  }

  @Test
  void shouldCreateUpdateAndDelete() {
    UUID id = UUID.randomUUID();
    CourseRequest request = new CourseRequest("CS", "Algorithms", 2, 6);
    Course domain = new Course(null, "CS", "Algorithms", 2, 6);
    Course saved = new Course(id, "CS", "Algorithms", 2, 6);
    CourseResponse response = new CourseResponse(id, "CS", "Algorithms", 2, 6);
    JCourse entity = new JCourse(id, "MA", "Math", 1, 4);
    when(mapper.toDomain(request)).thenReturn(domain);
    when(mapper.toResponse(any(Course.class))).thenReturn(response);
    when(repository.save(any())).thenReturn(entity);
    when(mapper.toEntity(any(Course.class))).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(saved);
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.existsById(id)).thenReturn(true);
    assertEquals(id, service.create(request).getId());
    assertEquals("CS", service.update(id, request).getReference());
    service.delete(id);
    verify(repository).deleteById(id);
  }

  @Test
  void shouldRejectMissingCoursesAndInvalidPagination() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    when(repository.existsById(id)).thenReturn(false);
    assertThrows(NotFoundException.class, () -> service.get(id));
    assertThrows(
        NotFoundException.class,
        () -> service.update(id, new CourseRequest("CS", "Algorithms", 2, 6)));
    assertThrows(NotFoundException.class, () -> service.delete(id));
    doThrow(new BadRequestException("invalid page")).when(paginationValidator).validate(-1, 20);
    assertThrows(BadRequestException.class, () -> service.list(null, null, -1, 20));
  }
}
