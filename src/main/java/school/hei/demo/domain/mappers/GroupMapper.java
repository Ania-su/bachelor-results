package school.hei.demo.domain.mappers;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.entity.Group;
import school.hei.demo.repository.model.JGroup;

@Component
public class GroupMapper {

  public GroupResponse toResponse(Group group) {
    if (group == null) return null;
    return GroupResponse.builder()
        .id(group.getId())
        .reference(group.getReference())
        .academicYear(group.getAcademicYear())
        .build();
  }

  public Group toDomain(GroupRequest request) {
    if (request == null) return null;
    return Group.builder()
        .reference(request.getReference())
        .academicYear(request.getAcademicYear())
        .build();
  }

  public Group toDomain(JGroup jGroup) {
    if (jGroup == null) return null;
    return Group.builder()
        .id(jGroup.getId())
        .reference(jGroup.getReference())
        .academicYear(jGroup.getAcademicYear())
        .build();
  }

  public JGroup toEntity(Group group) {
    if (group == null) return null;
    return JGroup.builder()
        .id(group.getId())
        .reference(group.getReference())
        .academicYear(group.getAcademicYear())
        .build();
  }
}
