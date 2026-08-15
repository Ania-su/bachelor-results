package school.hei.demo.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.entity.Group;

@Component
public class GroupRestMapper {

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
}
