package school.hei.demo.service;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import school.hei.demo.endpoint.rest.controller.validator.CourseValidator;
import school.hei.demo.entity.Course;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.mapper.CourseMapper;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository repository;
  private final CourseMapper mapper;
  private final CourseValidator validator;

  public Page<Course> list(Integer semester, String search, int page, int pageSize) {
    return repository
        .findAllFiltered(semester, search, PageRequest.of(page, pageSize))
        .map(mapper::toDomain);
  }

  public Course get(UUID id) {
    return repository
        .findById(id)
        .map(mapper::toDomain)
        .orElseThrow(() -> new NotFoundException("Course " + id + " not found"));
  }

  public Course create(Course course) {
    validator.validate(course);
    return mapper.toDomain(repository.save(mapper.toEntity(course)));
  }

  public Course update(UUID id, Course updated) {
    validator.validate(updated);
    var existing =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Course " + id + " not found"));
    existing.setReference(updated.getReference());
    existing.setTitle(updated.getTitle());
    existing.setSemester(updated.getSemester());
    existing.setCredits(updated.getCredits());
    return mapper.toDomain(repository.save(existing));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("Course " + id + " not found");
    }
    repository.deleteById(id);
  }
}
