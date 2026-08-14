package school.hei.demo.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.entity.Course;

@Component
public class CourseRestMapper {

  public CourseResponse toResponse(Course course) {
    if (course == null) return null;
    return CourseResponse.builder()
        .id(course.getId())
        .reference(course.getReference())
        .title(course.getTitle())
        .semester(course.getSemester())
        .credits(course.getCredits())
        .build();
  }

  public Course toDomain(CourseRequest request) {
    return Course.builder()
        .reference(request.getReference())
        .title(request.getTitle())
        .semester(request.getSemester())
        .credits(request.getCredits())
        .build();
  }
}
