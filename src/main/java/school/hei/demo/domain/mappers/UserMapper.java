package school.hei.demo.domain.mappers;

import org.springframework.stereotype.Component;
import school.hei.demo.entity.User;
import school.hei.demo.repository.model.JUser;

@Component
public class UserMapper {
  public User toDomain(JUser jUser) {
    if (jUser == null) {
      return null;
    }

    return new User(
        jUser.getId(),
        jUser.getReference(),
        jUser.getFirstName(),
        jUser.getLastName(),
        jUser.getEmail(),
        jUser.getPassword(),
        jUser.getUserRole(),
        jUser.getSpecialtyId(),
        jUser.getEntryYear(),
        jUser.getCreatedAt());
  }

  public JUser toJpa(User user) {
    if (user == null) {
      return null;
    }

    JUser jUser = new JUser();
    jUser.setId(user.getId());
    jUser.setReference(user.getReference());
    jUser.setFirstName(user.getFirstName());
    jUser.setLastName(user.getLastName());
    jUser.setEmail(user.getEmail());
    jUser.setPassword(user.getPassword());
    jUser.setUserRole(user.getUserRole());
    jUser.setSpecialtyId(user.getSpecialtyId());
    jUser.setEntryYear(user.getEntryYear());
    jUser.setCreatedAt(user.getCreatedAt());
    return jUser;
  }

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
