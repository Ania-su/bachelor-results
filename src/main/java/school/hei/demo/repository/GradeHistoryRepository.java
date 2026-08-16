package school.hei.demo.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.demo.repository.model.JGradeHistory;

public interface GradeHistoryRepository extends JpaRepository<JGradeHistory, UUID> {
  void deleteAllByGradeId(UUID gradeId);
}
