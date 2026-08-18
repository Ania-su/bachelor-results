package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.ExamRequest;
import school.hei.demo.domain.dto.response.ExamResponse;
import school.hei.demo.endpoint.rest.controller.mapper.ExamRestMapper;
import school.hei.demo.service.ExamService;

@RestController
@RequestMapping("/courses/{courseId}/exams")
@AllArgsConstructor
public class ExamController {

  private final ExamService service;
  private final ExamRestMapper mapper;

  @GetMapping
  public List<ExamResponse> listExams(@PathVariable UUID courseId) {
    var body = service.listForCourse(courseId).stream().map(mapper::toResponse).toList();
    return body;
  }

  @GetMapping("/{examId}")
  public ExamResponse getExam(@PathVariable UUID courseId, @PathVariable UUID examId) {
    return mapper.toResponse(service.get(courseId, examId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExamResponse createExam(@PathVariable UUID courseId, @RequestBody ExamRequest request) {
    var created = mapper.toResponse(service.create(courseId, mapper.toDomain(request)));
    return created;
  }

  @PatchMapping("/{examId}")
  public ExamResponse updateExam(
      @PathVariable UUID courseId, @PathVariable UUID examId, @RequestBody ExamRequest request) {
    var updated = mapper.toResponse(service.update(courseId, examId, mapper.toDomain(request)));
    return updated;
  }

  @DeleteMapping("/{examId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExam(@PathVariable UUID courseId, @PathVariable UUID examId) {
    service.delete(courseId, examId);
  }
}
