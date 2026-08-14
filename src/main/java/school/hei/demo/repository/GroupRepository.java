package school.hei.demo.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.demo.repository.model.JGroup;

public interface GroupRepository extends JpaRepository<JGroup, UUID> {

  @Query(
      """
      SELECT g FROM JGroup g
      WHERE (:academicYear IS NULL OR g.academicYear = :academicYear)
      """)
  Page<JGroup> findAllFiltered(@Param("academicYear") Integer academicYear, Pageable pageable);
}
