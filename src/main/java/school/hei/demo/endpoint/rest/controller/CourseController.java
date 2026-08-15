package school.hei.demo.endpoint.rest.controller;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.domain.dto.response.CoursePage;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.endpoint.rest.controller.mapper.CourseRestMapper;
import school.hei.demo.service.CourseService;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

  private final CourseService service;
  private final CourseRestMapper mapper;

  @GetMapping
  public CoursePage listCourses(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Integer semester,
      @RequestParam(required = false) String search) {

    var result = service.list(semester, search, page, pageSize);

    var body = result.getContent().stream().map(mapper::toResponse).toList();
    return new CoursePage(
        body,
        new PageMetadata(
            result.getNumber(),
            result.getSize(),
            Math.toIntExact(result.getTotalElements()),
            result.getTotalPages()));
  }

  @GetMapping("/{courseId}")
  public CourseResponse getCourse(@PathVariable UUID courseId) {
    return mapper.toResponse(service.get(courseId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseResponse createCourse(@RequestBody CourseRequest request) {
    return mapper.toResponse(service.create(mapper.toDomain(request)));
  }

  @PatchMapping("/{courseId}")
  public CourseResponse updateCourse(
      @PathVariable UUID courseId, @RequestBody CourseRequest request) {
    return mapper.toResponse(service.update(courseId, mapper.toDomain(request)));
  }

  @DeleteMapping("/{courseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCourse(@PathVariable UUID courseId) {
    service.delete(courseId);
  }
}
