package school.hei.demo.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.demo.enums.CodeType;
import school.hei.demo.repository.model.JCourseSpecialty;

public interface CourseSpecialtyRepository extends JpaRepository<JCourseSpecialty, UUID> {

  List<JCourseSpecialty> findByCourseId(UUID courseId);

  boolean existsByCourseIdAndSpecialtyId(UUID courseId, UUID specialtyId);

  @Query(
      """
      SELECT DISTINCT cs.course.id FROM JCourseSpecialty cs
      WHERE cs.specialty.id = :specialtyId
         OR cs.specialty.code = :noneCode
      """)
  Set<UUID> findRequiredCourseIds(
      @Param("specialtyId") UUID specialtyId, @Param("noneCode") CodeType noneCode);
}
