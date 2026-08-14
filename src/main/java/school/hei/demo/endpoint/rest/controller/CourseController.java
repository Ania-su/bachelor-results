package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.endpoint.rest.controller.mapper.CourseRestMapper;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.service.CourseService;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

  private final CourseService service;
  private final CourseRestMapper mapper;

  @GetMapping
  public ResponseEntity<List<CourseResponse>> listCourses(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Integer semester,
      @RequestParam(required = false) String search) {

    var result = service.list(semester, search, page, pageSize);

    var body = result.getContent().stream().map(mapper::toResponse).toList();

    var headers = new HttpHeaders();
    headers.add("X-Page", String.valueOf(page));
    headers.add("X-Page-Size", String.valueOf(pageSize));
    headers.add("X-Total-Elements", String.valueOf(result.getTotalElements()));
    headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));

    return ResponseEntity.ok().headers(headers).body(body);
  }

  @GetMapping("/{courseId}")
  public ResponseEntity<CourseResponse> getCourse(@PathVariable UUID courseId) {
    return ResponseEntity.ok(mapper.toResponse(service.get(courseId)));
  }

  @PostMapping
  public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseRequest request) {
    var created = mapper.toResponse(service.create(mapper.toDomain(request)));
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PatchMapping("/{courseId}")
  public ResponseEntity<CourseResponse> updateCourse(
      @PathVariable UUID courseId, @RequestBody CourseRequest request) {
    var updated = mapper.toResponse(service.update(courseId, mapper.toDomain(request)));
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{courseId}")
  public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
    service.delete(courseId);
    return ResponseEntity.noContent().build();
  }
}
