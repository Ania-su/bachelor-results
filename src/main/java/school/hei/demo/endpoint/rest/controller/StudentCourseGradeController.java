package school.hei.demo.endpoint.rest.controller;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.demo.domain.dto.response.StudentCourseGradeResponse;
import school.hei.demo.service.StudentCourseGradeService;

@RestController
@RequestMapping("/courses/{courseId}/student-grade/{student-id}")
@AllArgsConstructor
public class StudentCourseGradeController {
  private final StudentCourseGradeService service;

  @GetMapping
  public StudentCourseGradeResponse getStudentCourseGrade(
      @PathVariable UUID courseId, @PathVariable("student-id") UUID studentId) {
    return service.get(courseId, studentId);
  }
}
