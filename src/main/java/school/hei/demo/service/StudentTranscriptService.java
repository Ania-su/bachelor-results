package school.hei.demo.service;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.demo.domain.dto.response.CourseAverageResponse;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.YearTranscriptResponse;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;

@Service
@AllArgsConstructor
public class StudentTranscriptService {
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final GradeRepository gradeRepository;
  private final UserMapper userMapper;
  private final StudentCourseGradeService studentCourseGradeService;

  @Transactional(readOnly = true)
  public StudentTranscriptResponse get(UUID studentId, Integer year) {
    var user =
        userRepository
            .findById(studentId)
            .map(userMapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Student " + studentId + " not found"));

    var courseData =
        courseRepository.findAllForTranscript(user.getSpecialtyId()).stream()
            .map(
                course -> {
                  var courseGrade = studentCourseGradeService.get(course.getId(), studentId);
                  var hasAllGrades =
                      !courseGrade.getExams().isEmpty()
                          && gradeRepository.countByStudent_IdAndExam_Course_Id(
                                  studentId, course.getId())
                              == courseGrade.getExams().size();
                  return new CourseTranscriptData(
                      new CourseAverageResponse(
                          courseGrade.getCourse().getId(),
                          courseGrade.getCourse().getReference(),
                          courseGrade.getCourse().getTitle(),
                          courseGrade.getCourse().getSemester(),
                          courseGrade.getCourse().getCredits(),
                          courseGrade.getAverage()),
                      hasAllGrades);
                })
            .toList();

    var courses =
        courseData.stream()
            .filter(data -> year == null || (data.course().getSemester() + 1) / 2 == year)
            .map(CourseTranscriptData::course)
            .sorted(Comparator.comparing(CourseAverageResponse::getSemester))
            .toList();

    var years =
        courses.stream()
            .collect(Collectors.groupingBy(course -> (course.getSemester() + 1) / 2))
            .entrySet()
            .stream()
            .sorted(java.util.Map.Entry.comparingByKey())
            .map(
                entry -> {
                  var yearCourses = entry.getValue();
                  var totalCredits =
                      yearCourses.stream().mapToInt(CourseAverageResponse::getCredits).sum();
                  var weightedAverage =
                      totalCredits == 0
                          ? 0.0
                          : yearCourses.stream()
                                  .mapToDouble(course -> course.getAverage() * course.getCredits())
                                  .sum()
                              / totalCredits;
                  return new YearTranscriptResponse(entry.getKey(), yearCourses, weightedAverage);
                })
            .toList();

    var isOfficial =
        !courses.isEmpty()
            && courseData.stream()
                .filter(data -> year == null || (data.course().getSemester() + 1) / 2 == year)
                .allMatch(CourseTranscriptData::hasAllGrades);

    var totalCredits = courses.stream().mapToInt(CourseAverageResponse::getCredits).sum();
    var validatedCredits =
        courses.stream()
            .filter(course -> course.getAverage() >= 10)
            .mapToInt(CourseAverageResponse::getCredits)
            .sum();

    return new StudentTranscriptResponse(
        toResponse(user), years, isOfficial, totalCredits, validatedCredits);
  }

  private record CourseTranscriptData(CourseAverageResponse course, boolean hasAllGrades) {}

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
