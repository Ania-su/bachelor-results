package school.hei.demo.domain.mappers;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.entity.Course;
import school.hei.demo.repository.model.JCourse;

@Component
public class CourseMapper {

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
    if (request == null) return null;
    return Course.builder()
        .reference(request.getReference())
        .title(request.getTitle())
        .semester(request.getSemester())
        .credits(request.getCredits())
        .build();
  }

  public Course toDomain(JCourse jCourse) {
    if (jCourse == null) return null;
    return Course.builder()
        .id(jCourse.getId())
        .reference(jCourse.getReference())
        .title(jCourse.getTitle())
        .semester(jCourse.getSemester())
        .credits(jCourse.getCredits())
        .build();
  }

  public JCourse toEntity(Course course) {
    if (course == null) return null;
    return JCourse.builder()
        .id(course.getId())
        .reference(course.getReference())
        .title(course.getTitle())
        .semester(course.getSemester())
        .credits(course.getCredits())
        .build();
  }
}
