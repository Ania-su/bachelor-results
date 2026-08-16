package school.hei.demo.service;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.demo.domain.dto.response.CourseAverageResponse;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.UserRepository;

@Service
@AllArgsConstructor
public class StudentTranscriptService {
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final UserMapper userMapper;
  private final StudentCourseGradeService studentCourseGradeService;

  @Transactional(readOnly = true)
  public StudentTranscriptResponse get(UUID studentId) {
    var user =
        userRepository
            .findById(studentId)
            .map(userMapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Student " + studentId + " not found"));

    var courses =
        courseRepository.findAll().stream()
            .map(
                course -> {
                  var courseGrade = studentCourseGradeService.get(course.getId(), studentId);
                  return new CourseAverageResponse(courseGrade.getCourse(), courseGrade.getAverage());
                })
            .toList();

    return new StudentTranscriptResponse(toResponse(user), courses);
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
