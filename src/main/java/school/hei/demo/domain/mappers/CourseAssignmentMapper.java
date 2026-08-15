package school.hei.demo.domain.mappers;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.CourseAssignment;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.repository.model.JCourseAssignment;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CourseAssignmentMapper {

    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;

    public CourseAssignment toDomain(JCourseAssignment j) {
        if (j == null) return null;
        return CourseAssignment.builder()
                .id(j.getId())
                .courseId(j.getCourse().getId())
                .teacherId(j.getTeacherId())
                .groupId(j.getGroup().getId())
                .build();
    }

    public JCourseAssignment toEntity(CourseAssignment a) {
        if (a == null) return null;
        return JCourseAssignment.builder()
                .id(a.getId())
                .course(courseRepository.getReferenceById(a.getCourseId()))
                .teacherId(a.getTeacherId())
                .group(groupRepository.getReferenceById(a.getGroupId()))
                .build();
    }
}
