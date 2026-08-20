package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.GradeCreate;
import school.hei.demo.domain.dto.request.GradeUpdate;
import school.hei.demo.domain.dto.response.GradeHistoryResponse;
import school.hei.demo.domain.dto.response.GradePage;
import school.hei.demo.domain.dto.response.GradeResponse;
import school.hei.demo.service.GradeHistoryService;
import school.hei.demo.service.GradeService;

@RestController
@RequestMapping("/courses/{courseId}/exams/{examId}/grades")
@AllArgsConstructor
public class GradeController {
  private final GradeService gradeService;
  private final GradeHistoryService gradeHistoryService;

  @GetMapping
  public GradePage listGrades(
      @PathVariable UUID courseId,
      @PathVariable UUID examId,
      @RequestParam(required = false) UUID studentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    return gradeService.list(courseId, examId, studentId, page, pageSize);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GradeResponse createGrade(
      @PathVariable UUID courseId,
      @PathVariable UUID examId,
      @RequestBody(required = false) GradeCreate request) {
    return gradeService.create(courseId, examId, request);
  }

  @GetMapping("/{gradeId}")
  public GradeResponse getGrade(
      @PathVariable UUID courseId, @PathVariable UUID examId, @PathVariable UUID gradeId) {
    return gradeService.get(courseId, examId, gradeId);
  }

  @PatchMapping("/{gradeId}")
  public GradeResponse updateGrade(
      @PathVariable UUID courseId,
      @PathVariable UUID examId,
      @PathVariable UUID gradeId,
      @RequestBody(required = false) GradeUpdate request) {
    return gradeService.update(courseId, examId, gradeId, request);
  }

  @GetMapping("/history")
  public List<GradeHistoryResponse> listGradeHistory(
      @PathVariable UUID courseId, @PathVariable UUID examId) {
    return gradeHistoryService.listForExam(examId);
  }

  @DeleteMapping("/{gradeId}")
  public GradeResponse deleteGrade(
      @PathVariable UUID courseId, @PathVariable UUID examId, @PathVariable UUID gradeId) {
    return gradeService.delete(courseId, examId, gradeId);
  }
}
