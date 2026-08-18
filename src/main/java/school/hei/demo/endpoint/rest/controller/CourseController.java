package school.hei.demo.endpoint.rest.controller;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.domain.dto.response.CoursePage;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.service.CourseService;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

  private final CourseService service;

  @GetMapping
  public CoursePage listCourses(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Integer semester,
      @RequestParam(required = false) String search) {
    return service.list(semester, search, page, pageSize);
  }

  @GetMapping("/{courseId}")
  public CourseResponse getCourse(@PathVariable UUID courseId) {
    return service.get(courseId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseResponse createCourse(@RequestBody CourseRequest request) {
    return service.create(request);
  }

  @PatchMapping("/{courseId}")
  public CourseResponse updateCourse(
      @PathVariable UUID courseId, @RequestBody CourseRequest request) {
    return service.update(courseId, request);
  }

  @DeleteMapping("/{courseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCourse(@PathVariable UUID courseId) {
    service.delete(courseId);
  }
}
