package school.hei.demo.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.demo.repository.model.JCourse;

public interface CourseRepository extends JpaRepository<JCourse, UUID> {

  @Query(
      """
      SELECT c FROM JCourse c
      WHERE (:semester IS NULL OR c.semester = :semester)
        AND (:search IS NULL OR
             LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
             LOWER(c.reference) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
      """)
  Page<JCourse> findAllFiltered(
      @Param("semester") Integer semester, @Param("search") String search, Pageable pageable);
}
