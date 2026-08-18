package school.hei.demo.endpoint.rest.controller.mapper;

import org.springframework.stereotype.Component;
import school.hei.demo.repository.model.JUser;

@Component
public class UserRestMapper {

  public school.hei.demo.domain.dto.response.User toResponse(JUser u) {
    return new school.hei.demo.domain.dto.response.User(
        u.getId(),
        u.getReference(),
        u.getFirstName(),
        u.getLastName(),
        u.getEmail(),
        u.getUserRole(),
        u.getSpecialtyId(),
        u.getEntryYear(),
        u.getCreatedAt());
  }
}
