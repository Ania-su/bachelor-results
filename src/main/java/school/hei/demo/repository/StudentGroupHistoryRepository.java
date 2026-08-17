package school.hei.demo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.demo.repository.model.JStudentGroupHistory;

public interface StudentGroupHistoryRepository extends JpaRepository<JStudentGroupHistory, UUID> {

  @Query(
      """
      SELECT h FROM JStudentGroupHistory h
      WHERE h.group.id = :groupId AND h.endDate IS NULL
      """)
  List<JStudentGroupHistory> findCurrentByGroupId(@Param("groupId") UUID groupId);

  @Query(
      """
      SELECT h FROM JStudentGroupHistory h
      WHERE h.student.id = :studentId AND h.endDate IS NULL
      """)
  Optional<JStudentGroupHistory> findCurrentByStudentId(@Param("studentId") UUID studentId);

  List<JStudentGroupHistory> findAllByStudent_IdOrderByStartDateDesc(UUID studentId);
}
