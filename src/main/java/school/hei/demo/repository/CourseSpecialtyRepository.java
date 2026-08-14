package school.hei.demo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.demo.repository.model.JCourseSpecialty;

public interface CourseSpecialtyRepository extends JpaRepository<JCourseSpecialty, UUID> {

  List<JCourseSpecialty> findByCourseId(UUID courseId);

  boolean existsByCourseIdAndSpecialtyId(UUID courseId, UUID specialtyId);
}
