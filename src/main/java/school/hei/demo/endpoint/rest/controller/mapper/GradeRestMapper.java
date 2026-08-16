package school.hei.demo.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.GradeCreate;
import school.hei.demo.domain.dto.response.GradeResponse;
import school.hei.demo.entity.Grade;

@Component
public class GradeRestMapper {
  public GradeResponse toResponse(Grade grade) {
    if (grade == null) return null;
    return GradeResponse.builder()
        .id(grade.getId())
        .studentId(grade.getStudentId())
        .examId(grade.getExamId())
        .value(grade.getValue())
        .updatedAt(grade.getUpdatedAt())
        .build();
  }

  public Grade toDomain(GradeCreate request) {
    if (request == null) return null;
    return Grade.builder().studentId(request.getStudentId()).value(request.getValue()).build();
  }
}
