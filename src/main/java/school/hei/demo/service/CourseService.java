package school.hei.demo.service;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.domain.dto.response.CoursePage;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.domain.mappers.CourseMapper;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.validators.CourseValidator;
import school.hei.demo.validators.PaginationValidator;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository repository;
  private final CourseMapper mapper;
  private final CourseValidator validator;
  private final PaginationValidator paginationValidator;

  public CoursePage list(Integer semester, String search, int page, int pageSize) {
    paginationValidator.validate(page, pageSize);
    var result =
        repository
            .findAllFiltered(semester, search, PageRequest.of(page, pageSize))
            .map(mapper::toDomain);
    var body = result.getContent().stream().map(mapper::toResponse).toList();
    return new CoursePage(
        body,
        new PageMetadata(
            result.getNumber(),
            result.getSize(),
            Math.toIntExact(result.getTotalElements()),
            result.getTotalPages()));
  }

  public CourseResponse get(UUID id) {
    return mapper.toResponse(
        repository
            .findById(id)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Course " + id + " not found")));
  }

  public CourseResponse create(CourseRequest request) {
    var course = mapper.toDomain(request);
    validator.validate(course);
    return mapper.toResponse(mapper.toDomain(repository.save(mapper.toEntity(course))));
  }

  public CourseResponse update(UUID id, CourseRequest request) {
    var updated = mapper.toDomain(request);
    validator.validate(updated);
    var existing =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Course " + id + " not found"));
    existing.setReference(updated.getReference());
    existing.setTitle(updated.getTitle());
    existing.setSemester(updated.getSemester());
    existing.setCredits(updated.getCredits());
    return mapper.toResponse(mapper.toDomain(repository.save(existing)));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("Course " + id + " not found");
    }
    repository.deleteById(id);
  }
}
