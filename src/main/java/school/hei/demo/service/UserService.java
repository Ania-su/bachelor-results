package school.hei.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.exception.ConflictException;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.entity.JUser;
import school.hei.demo.validators.UserValidator;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserValidator userValidator;

  public User create(UserCreate request) {
    userValidator.validate(request);

    if (userRepository.existsByReference(request.reference())) {
      throw new ConflictException("A user with this reference already exists");
    }
    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new ConflictException("A user with this email already exists");
    }

    school.hei.demo.domain.entity.User user =
        new school.hei.demo.domain.entity.User(
            null,
            request.reference(),
            request.firstName(),
            request.lastName(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.userRole(),
            request.specialtyId(),
            request.entryYear(),
            null);

    JUser savedUser = userRepository.save(userMapper.toJpa(user));
    school.hei.demo.domain.entity.User savedDomainUser = userMapper.toDomain(savedUser);

    return new User(
        savedDomainUser.getId(),
        savedDomainUser.getReference(),
        savedDomainUser.getFirstName(),
        savedDomainUser.getLastName(),
        savedDomainUser.getEmail(),
        savedDomainUser.getUserRole(),
        savedDomainUser.getSpecialtyId(),
        savedDomainUser.getEntryYear(),
        savedDomainUser.getCreatedAt());
  }
}
