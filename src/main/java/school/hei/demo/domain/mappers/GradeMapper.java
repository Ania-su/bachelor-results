package school.hei.demo.domain.mappers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.demo.entity.Grade;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JGrade;

@Component
@AllArgsConstructor
public class GradeMapper {
  private final UserRepository userRepository;
  private final ExamRepository examRepository;

  public Grade toDomain(JGrade grade) {
    if (grade == null) return null;
    return Grade.builder()
        .id(grade.getId())
        .studentId(grade.getStudent().getId())
        .examId(grade.getExam().getId())
        .value(grade.getValue())
        .updatedAt(grade.getUpdatedAt())
        .build();
  }

  public JGrade toEntity(Grade grade) {
    if (grade == null) return null;
    return JGrade.builder()
        .id(grade.getId())
        .student(userRepository.getReferenceById(grade.getStudentId()))
        .exam(examRepository.getReferenceById(grade.getExamId()))
        .value(grade.getValue())
        .updatedAt(grade.getUpdatedAt())
        .build();
  }
}
