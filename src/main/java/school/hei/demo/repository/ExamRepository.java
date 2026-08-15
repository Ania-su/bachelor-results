package school.hei.demo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.demo.repository.model.JExam;

public interface ExamRepository extends JpaRepository<JExam, UUID> {

  List<JExam> findAllByCourse_Id(UUID courseId);
}
