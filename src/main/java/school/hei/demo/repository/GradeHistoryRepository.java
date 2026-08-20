package school.hei.demo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.demo.repository.model.JGradeHistory;

public interface GradeHistoryRepository extends JpaRepository<JGradeHistory, UUID> {
  void deleteAllByGradeId(UUID gradeId);

  @Query(
      """
      SELECT h FROM JGradeHistory h
      WHERE h.gradeId IN (SELECT g.id FROM JGrade g WHERE g.exam.id = :examId)
      ORDER BY h.changedAt DESC
      """)
  List<JGradeHistory> findAllByExamId(@Param("examId") UUID examId);
}
