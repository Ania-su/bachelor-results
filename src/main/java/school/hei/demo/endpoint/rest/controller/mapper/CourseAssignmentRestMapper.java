package school.hei.demo.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.CourseAssignmentRequest;
import school.hei.demo.domain.dto.response.CourseAssignmentResponse;
import school.hei.demo.entity.CourseAssignment;

@Component
public class CourseAssignmentRestMapper {

    public CourseAssignmentResponse toResponse(CourseAssignment a) {
        if (a == null) return null;
        return CourseAssignmentResponse.builder()
                .id(a.getId())
                .courseId(a.getCourseId())
                .teacherId(a.getTeacherId())
                .groupId(a.getGroupId())
                .build();
    }

    public CourseAssignment toDomain(CourseAssignmentRequest request) {
        return CourseAssignment.builder()
                .teacherId(request.getTeacherId())
                .groupId(request.getGroupId())
                .build();
    }
}
