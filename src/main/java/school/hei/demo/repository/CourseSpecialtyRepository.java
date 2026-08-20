package school.hei.demo.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.demo.repository.model.JCourseSpecialty;

public interface CourseSpecialtyRepository extends JpaRepository<JCourseSpecialty, UUID> {

  List<JCourseSpecialty> findByCourseId(UUID courseId);

  boolean existsByCourseIdAndSpecialtyId(UUID courseId, UUID specialtyId);

  @Query(
      """
      SELECT DISTINCT ca.course.id
      FROM JCourseAssignment ca
      JOIN JStudentGroupHistory sgh ON sgh.group.id = ca.group.id
      WHERE sgh.student.id = :studentId
        AND (
            NOT EXISTS (
                SELECT 1
                FROM JCourseSpecialty cs
                WHERE cs.course.id = ca.course.id
            )
            OR EXISTS (
                SELECT 1
                FROM JCourseSpecialty cs
                WHERE cs.course.id = ca.course.id
                  AND cs.specialty.id = :specialtyId
            )
        )
      """)
  Set<UUID> findRequiredCourseIds(
      @Param("studentId") UUID studentId, @Param("specialtyId") UUID specialtyId);
}
