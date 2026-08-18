package school.hei.demo.domain.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.ExamRequest;
import school.hei.demo.domain.dto.response.ExamResponse;
import school.hei.demo.entity.Exam;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.model.JExam;

@Component
@AllArgsConstructor
public class ExamMapper {

  private final CourseRepository courseRepository;

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

  public Exam toDomain(JExam j) {
    if (j == null) return null;
    return Exam.builder()
        .id(j.getId())
        .courseId(j.getCourse().getId())
        .dateExam(j.getDateExam())
        .coef(j.getCoef())
        .build();
  }

  public JExam toEntity(Exam e) {
    if (e == null) return null;
    return JExam.builder()
        .id(e.getId())
        .course(courseRepository.getReferenceById(e.getCourseId()))
        .dateExam(e.getDateExam())
        .coef(e.getCoef())
        .build();
  }
}
