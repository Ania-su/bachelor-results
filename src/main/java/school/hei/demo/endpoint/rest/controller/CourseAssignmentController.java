package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.CourseAssignmentRequest;
import school.hei.demo.domain.dto.response.CourseAssignmentResponse;
import school.hei.demo.endpoint.rest.controller.mapper.CourseAssignmentRestMapper;
import school.hei.demo.service.CourseAssignmentService;

@RestController
@RequestMapping("/courses/{courseId}/assignments")
@AllArgsConstructor
public class CourseAssignmentController {

  private final CourseAssignmentService service;
  private final CourseAssignmentRestMapper mapper;

  @GetMapping
  public ResponseEntity<List<CourseAssignmentResponse>> listAssignments(
      @PathVariable UUID courseId) {
    var body = service.listForCourse(courseId).stream().map(mapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{assignmentId}")
  public ResponseEntity<CourseAssignmentResponse> getAssignment(
      @PathVariable UUID courseId, @PathVariable UUID assignmentId) {
    return ResponseEntity.ok(mapper.toResponse(service.get(courseId, assignmentId)));
  }

  @PostMapping
  public ResponseEntity<CourseAssignmentResponse> createAssignment(
      @PathVariable UUID courseId, @RequestBody CourseAssignmentRequest request) {
    var created = mapper.toResponse(service.create(courseId, mapper.toDomain(request)));
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PatchMapping("/{assignmentId}")
  public ResponseEntity<CourseAssignmentResponse> updateAssignment(
      @PathVariable UUID courseId,
      @PathVariable UUID assignmentId,
      @RequestBody CourseAssignmentRequest request) {
    var updated =
        mapper.toResponse(service.update(courseId, assignmentId, mapper.toDomain(request)));
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{assignmentId}")
  public ResponseEntity<Void> deleteAssignment(
      @PathVariable UUID courseId, @PathVariable UUID assignmentId) {
    service.delete(courseId, assignmentId);
    return ResponseEntity.noContent().build();
  }
}
