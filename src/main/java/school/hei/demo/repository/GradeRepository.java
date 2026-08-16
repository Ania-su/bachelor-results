package school.hei.demo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.demo.repository.model.JGrade;

public interface GradeRepository extends JpaRepository<JGrade, UUID> {
  boolean existsByStudent_IdAndExam_Id(UUID studentId, UUID examId);

  Page<JGrade> findAllByExam_Id(UUID examId, Pageable pageable);

  Page<JGrade> findAllByExam_IdAndStudent_Id(UUID examId, UUID studentId, Pageable pageable);

  List<JGrade> findAllByStudent_IdAndExam_Course_Id(UUID studentId, UUID courseId);
}
