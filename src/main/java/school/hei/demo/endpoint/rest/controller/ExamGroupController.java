package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.ExamGroupRequest;
import school.hei.demo.domain.dto.response.ExamGroupResponse;
import school.hei.demo.service.ExamGroupService;

@RestController
@RequestMapping("/courses/{courseId}/exams/{examId}/groups")
@AllArgsConstructor
public class ExamGroupController {

  private final ExamGroupService service;

  @GetMapping
  public List<ExamGroupResponse> listExamGroups(
      @PathVariable UUID courseId, @PathVariable UUID examId) {
    return service.listForExam(courseId, examId);
  }

  @GetMapping("/{examGroupId}")
  public ExamGroupResponse getExamGroup(
      @PathVariable UUID courseId, @PathVariable UUID examId, @PathVariable UUID examGroupId) {
    return service.get(courseId, examId, examGroupId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExamGroupResponse createExamGroup(
      @PathVariable UUID courseId,
      @PathVariable UUID examId,
      @RequestBody ExamGroupRequest request) {
    return service.create(courseId, examId, request);
  }

  @DeleteMapping("/{examGroupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExamGroup(
      @PathVariable UUID courseId, @PathVariable UUID examId, @PathVariable UUID examGroupId) {
    service.delete(courseId, examId, examGroupId);
  }
}
