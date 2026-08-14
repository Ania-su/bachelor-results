package school.hei.demo.domain.mappers;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.Course;
import school.hei.demo.repository.model.JCourse;

@Component
public class CourseMapper {

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
