package school.hei.demo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.demo.repository.model.JExamGroup;

public interface ExamGroupRepository extends JpaRepository<JExamGroup, UUID> {

  List<JExamGroup> findAllByExam_Id(UUID examId);
}
