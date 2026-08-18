package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.CourseAssignmentRequest;
import school.hei.demo.domain.dto.response.CourseAssignmentResponse;
import school.hei.demo.service.CourseAssignmentService;

@RestController
@RequestMapping("/courses/{courseId}/assignments")
@AllArgsConstructor
public class CourseAssignmentController {

  private final CourseAssignmentService service;

  @GetMapping
  public List<CourseAssignmentResponse> listAssignments(@PathVariable UUID courseId) {
    return service.listForCourse(courseId);
  }

  @GetMapping("/{assignmentId}")
  public CourseAssignmentResponse getAssignment(
      @PathVariable UUID courseId, @PathVariable UUID assignmentId) {
    return service.get(courseId, assignmentId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseAssignmentResponse createAssignment(
      @PathVariable UUID courseId, @RequestBody CourseAssignmentRequest request) {
    return service.create(courseId, request);
  }

  @PatchMapping("/{assignmentId}")
  public CourseAssignmentResponse updateAssignment(
      @PathVariable UUID courseId,
      @PathVariable UUID assignmentId,
      @RequestBody CourseAssignmentRequest request) {
    return service.update(courseId, assignmentId, request);
  }

  @DeleteMapping("/{assignmentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAssignment(@PathVariable UUID courseId, @PathVariable UUID assignmentId) {
    service.delete(courseId, assignmentId);
  }
}
