package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.ExamGroupRequest;
import school.hei.demo.domain.dto.response.ExamGroupResponse;
import school.hei.demo.endpoint.rest.controller.mapper.ExamGroupRestMapper;
import school.hei.demo.service.ExamGroupService;

@RestController
@RequestMapping("/courses/{courseId}/exams/{examId}/groups")
@AllArgsConstructor
public class ExamGroupController {

  private final ExamGroupService service;
  private final ExamGroupRestMapper mapper;

  @GetMapping
  public ResponseEntity<List<ExamGroupResponse>> listExamGroups(
      @PathVariable UUID courseId, @PathVariable UUID examId) {
    var body = service.listForExam(examId).stream().map(mapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{examGroupId}")
  public ResponseEntity<ExamGroupResponse> getExamGroup(
      @PathVariable UUID courseId, @PathVariable UUID examId, @PathVariable UUID examGroupId) {
    return ResponseEntity.ok(mapper.toResponse(service.get(examId, examGroupId)));
  }

  @PostMapping
  public ResponseEntity<ExamGroupResponse> createExamGroup(
      @PathVariable UUID courseId,
      @PathVariable UUID examId,
      @RequestBody ExamGroupRequest request) {
    var created = mapper.toResponse(service.create(examId, mapper.toDomain(request)));
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @DeleteMapping("/{examGroupId}")
  public ResponseEntity<Void> deleteExamGroup(
      @PathVariable UUID courseId, @PathVariable UUID examId, @PathVariable UUID examGroupId) {
    service.delete(examId, examGroupId);
    return ResponseEntity.noContent().build();
  }
}
