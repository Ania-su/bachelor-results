package school.hei.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.UserPage;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.enums.UserRole;
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

    return toResponse(savedDomainUser);
  }

  public UserPage findAll(
      int page, int pageSize, UserRole role, String reference,
      String firstName, String lastName, String email) {
    userValidator.validatePagination(page, pageSize);
    Pageable pageable = PageRequest.of(page, pageSize);
    Page<JUser> users =
        userRepository.findAllByFilters(
            role,
            normalizeFilter(reference),
            normalizeFilter(firstName),
            normalizeFilter(lastName),
            normalizeFilter(email),
            pageable);

    Page<User> responsePage = users.map(this::mapToResponse);
    return new UserPage(
        responsePage.getContent(),
        new PageMetadata(
            responsePage.getNumber(),
            responsePage.getSize(),
            Math.toIntExact(responsePage.getTotalElements()),
            responsePage.getTotalPages()));
  }

  private User mapToResponse(JUser jUser) {
    return toResponse(userMapper.toDomain(jUser));
  }

  private User toResponse(school.hei.demo.domain.entity.User user) {
    return new User(
        user.getId(),
        user.getReference(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getUserRole(),
        user.getSpecialtyId(),
        user.getEntryYear(),
        user.getCreatedAt());
  }

  private String normalizeFilter(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
