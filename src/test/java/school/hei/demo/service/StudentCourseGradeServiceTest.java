package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.dto.response.ExamGradeResponse;
import school.hei.demo.domain.dto.response.StudentCourseGradeResponse;
import school.hei.demo.domain.mappers.CourseMapper;
import school.hei.demo.domain.mappers.ExamMapper;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.entity.Course;
import school.hei.demo.entity.Exam;
import school.hei.demo.entity.User;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JExam;
import school.hei.demo.repository.model.JGrade;
import school.hei.demo.repository.model.JUser;

@ExtendWith(MockitoExtension.class)
class StudentCourseGradeServiceTest {
  @Mock UserRepository userRepository;
  @Mock CourseRepository courseRepository;
  @Mock ExamRepository examRepository;
  @Mock GradeRepository gradeRepository;
  @Mock UserMapper userMapper;
  @Mock CourseMapper courseMapper;
  @Mock ExamMapper examMapper;
  @InjectMocks StudentCourseGradeService service;

  @Test
  void get_missingStudent_throws() {
    UUID courseId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.get(courseId, studentId));
  }

  @Test
  void get_missingCourse_throws() {
    UUID courseId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    JUser j = new JUser();
    j.setId(studentId);
    when(userRepository.findById(studentId)).thenReturn(Optional.of(j));
    when(userMapper.toDomain(j)).thenReturn(new User());
    when(courseRepository.findById(courseId)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.get(courseId, studentId));
  }

  @Test
  void get_withGrades_computesWeightedSum() {
    UUID courseId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    JUser j = new JUser();
    j.setId(studentId);
    JCourse jCourse = JCourse.builder().id(courseId).build();
    JExam jExam = JExam.builder().id(examId).course(jCourse).coef(new BigDecimal("2")).build();
    JGrade jGrade = JGrade.builder().exam(jExam).value(new BigDecimal("10")).build();

    User domainUser = new User();
    domainUser.setId(studentId);
    Course course =
        Course.builder()
            .id(courseId)
            .reference("REF")
            .title("Title")
            .semester(1)
            .credits(5)
            .build();
    Exam exam = Exam.builder().id(examId).courseId(courseId).coef(new BigDecimal("2")).build();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(j));
    when(userMapper.toDomain(j)).thenReturn(domainUser);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(jCourse));
    when(courseMapper.toDomain(jCourse)).thenReturn(course);
    when(examRepository.findAllByCourse_Id(courseId)).thenReturn(List.of(jExam));
    when(gradeRepository.findAllByStudent_IdAndExam_Course_Id(studentId, courseId))
        .thenReturn(List.of(jGrade));
    when(examMapper.toDomain(jExam)).thenReturn(exam);

    StudentCourseGradeResponse response = service.get(courseId, studentId);

    assertEquals(1, response.getExams().size());
    ExamGradeResponse examResponse = response.getExams().get(0);
    assertEquals(10.0, examResponse.getGrade());
    assertEquals(2.0, examResponse.getCoef());
    assertEquals(20.0, response.getAverage());
  }

  @Test
  void get_withoutGrade_defaultsToZero() {
    UUID courseId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    JUser j = new JUser();
    j.setId(studentId);
    JCourse jCourse = JCourse.builder().id(courseId).build();
    JExam jExam = JExam.builder().id(examId).course(jCourse).coef(new BigDecimal("3")).build();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(j));
    when(userMapper.toDomain(j)).thenReturn(new User());
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(jCourse));
    when(courseMapper.toDomain(jCourse)).thenReturn(Course.builder().id(courseId).build());
    when(examRepository.findAllByCourse_Id(courseId)).thenReturn(List.of(jExam));
    when(gradeRepository.findAllByStudent_IdAndExam_Course_Id(studentId, courseId))
        .thenReturn(List.of());
    when(examMapper.toDomain(jExam))
        .thenReturn(Exam.builder().id(examId).courseId(courseId).coef(new BigDecimal("3")).build());

    StudentCourseGradeResponse response = service.get(courseId, studentId);

    assertEquals(1, response.getExams().size());
    assertEquals(0.0, response.getExams().get(0).getGrade());
    assertEquals(0.0, response.getAverage());
  }
}
