package school.hei.demo.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.domain.dto.response.ExamGradeResponse;
import school.hei.demo.domain.dto.response.StudentCourseGradeResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.mappers.CourseMapper;
import school.hei.demo.domain.mappers.ExamMapper;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;

@Service
@AllArgsConstructor
public class StudentCourseGradeService {
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;
  private final UserMapper userMapper;
  private final CourseMapper courseMapper;
  private final ExamMapper examMapper;

  @Transactional(readOnly = true)
  public StudentCourseGradeResponse get(UUID courseId, UUID studentId) {
    var user =
        userRepository
            .findById(studentId)
            .map(userMapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Student " + studentId + " not found"));
    var course =
        courseRepository
            .findById(courseId)
            .map(courseMapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Course " + courseId + " not found"));

    var exams = examRepository.findAllByCourse_Id(courseId);
    Map<UUID, BigDecimal> gradesByExamId =
        gradeRepository.findAllByStudent_IdAndExam_Course_Id(studentId, courseId).stream()
            .collect(Collectors.toMap(grade -> grade.getExam().getId(), grade -> grade.getValue()));

    var examResponses =
        exams.stream()
            .map(examMapper::toDomain)
            .map(
                exam ->
                    new ExamGradeResponse(
                        exam.getId(),
                        exam.getCourseId(),
                        exam.getDateExam(),
                        exam.getCoef().doubleValue(),
                        gradesByExamId.getOrDefault(exam.getId(), BigDecimal.ZERO).doubleValue()))
            .toList();

    double average =
        exams.stream()
            .map(
                exam ->
                    exam.getCoef()
                        .multiply(gradesByExamId.getOrDefault(exam.getId(), BigDecimal.ZERO)))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue();

    return new StudentCourseGradeResponse(
        toResponse(user),
        new CourseResponse(
            course.getId(),
            course.getReference(),
            course.getTitle(),
            course.getSemester(),
            course.getCredits()),
        examResponses,
        average);
  }

  private User toResponse(school.hei.demo.entity.User user) {
    return new User(
        user.getId(),
        user.getReference(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getUserRole(),
        user.getSpecialtyId(),
        user.getEntryYear(),
        user.getCreatedAt());
  }
}
