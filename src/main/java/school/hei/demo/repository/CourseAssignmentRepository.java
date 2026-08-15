package school.hei.demo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.demo.repository.model.JCourseAssignment;

public interface CourseAssignmentRepository extends JpaRepository<JCourseAssignment, UUID> {

    List<JCourseAssignment> findAllByCourse_Id(UUID courseId);

    boolean existsByCourse_IdAndTeacherIdAndGroup_Id(UUID courseId, UUID teacherId, UUID groupId);
}
