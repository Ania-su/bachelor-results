package school.hei.demo.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.demo.entity.CourseAverageResult;
import school.hei.demo.repository.model.JCourse;
import school.hei.demo.repository.model.JGrade;

public interface GradeRepository extends JpaRepository<JGrade, UUID> {
  boolean existsByStudent_IdAndExam_Id(UUID studentId, UUID examId);

  Page<JGrade> findAllByExam_Id(UUID examId, Pageable pageable);

  Page<JGrade> findAllByExam_IdAndStudent_Id(UUID examId, UUID studentId, Pageable pageable);

  List<JGrade> findAllByStudent_IdAndExam_Course_Id(UUID studentId, UUID courseId);

  long countByStudent_IdAndExam_Course_Id(UUID studentId, UUID courseId);

  @Query(
      """
      SELECT DISTINCT g.exam.course
      FROM JGrade g
      WHERE g.student.id = :studentId
      """)
  List<JCourse> findCoursesWithGrades(@Param("studentId") UUID studentId);

  @Query(
      """
      SELECT new school.hei.demo.entity.CourseAverageResult(
          g.exam.course.id, g.exam.course.credits, SUM(g.value * g.exam.coef) / SUM(g.exam.coef))
      FROM JGrade g
      WHERE g.student.id = :studentId AND g.exam.course.id IN :courseIds
      GROUP BY g.exam.course.id, g.exam.course.credits
      """)
  List<CourseAverageResult> findCourseAverages(
      @Param("studentId") UUID studentId, @Param("courseIds") Set<UUID> courseIds);
}
