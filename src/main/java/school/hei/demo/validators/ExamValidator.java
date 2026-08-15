package school.hei.demo.validators;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import school.hei.demo.entity.Exam;

@Component
public class ExamValidator {

  public void validate(Exam exam) {
    if (exam.getCourseId() == null) {
      throw new IllegalArgumentException("courseId is required");
    }
    if (exam.getDateExam() == null) {
      throw new IllegalArgumentException("dateExam is required");
    }
    if (exam.getCoef() == null) {
      throw new IllegalArgumentException("coef is required");
    }
    if (exam.getCoef().compareTo(BigDecimal.ZERO) <= 0
        || exam.getCoef().compareTo(BigDecimal.ONE) > 0) {
      throw new IllegalArgumentException("coef must be strictly greater than 0 and at most 1");
    }
  }
}
