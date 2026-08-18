package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
  public List<ExamGroupResponse> listExamGroups(
      @PathVariable UUID courseId, @PathVariable UUID examId) {
    var body = service.listForExam(courseId, examId).stream().map(mapper::toResponse).toList();
    return body;
  }

  @GetMapping("/{examGroupId}")
  public ExamGroupResponse getExamGroup(
      @PathVariable UUID courseId, @PathVariable UUID examId, @PathVariable UUID examGroupId) {
    return mapper.toResponse(service.get(courseId, examId, examGroupId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExamGroupResponse createExamGroup(
      @PathVariable UUID courseId,
      @PathVariable UUID examId,
      @RequestBody ExamGroupRequest request) {
    var created = mapper.toResponse(service.create(courseId, examId, mapper.toDomain(request)));
    return created;
  }

  @DeleteMapping("/{examGroupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExamGroup(
      @PathVariable UUID courseId, @PathVariable UUID examId, @PathVariable UUID examGroupId) {
    service.delete(courseId, examId, examGroupId);
  }
}
