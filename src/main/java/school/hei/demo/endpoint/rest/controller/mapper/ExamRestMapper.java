package school.hei.demo.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.ExamRequest;
import school.hei.demo.domain.dto.response.ExamResponse;
import school.hei.demo.entity.Exam;

@Component
public class ExamRestMapper {

  public ExamResponse toResponse(Exam e) {
    if (e == null) return null;
    return ExamResponse.builder()
        .id(e.getId())
        .courseId(e.getCourseId())
        .dateExam(e.getDateExam())
        .coef(e.getCoef())
        .build();
  }

  public Exam toDomain(ExamRequest request) {
    return Exam.builder().dateExam(request.getDateExam()).coef(request.getCoef()).build();
  }
}
