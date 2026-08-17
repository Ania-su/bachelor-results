package school.hei.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.LoginRequest;
import school.hei.demo.domain.dto.response.AuthResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.exception.UnauthorizedException;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JUser;
import school.hei.demo.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResponse login(LoginRequest request) {
    JUser jUser =
        userRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new UnauthorizedException("This email is not linked to an account"));

    if (!passwordEncoder.matches(request.password(), jUser.getPassword())) {
      throw new UnauthorizedException("Invalid password");
    }

    school.hei.demo.entity.User domainUser = userMapper.toDomain(jUser);
    User user =
        new User(
            domainUser.getId(),
            domainUser.getReference(),
            domainUser.getFirstName(),
            domainUser.getLastName(),
            domainUser.getEmail(),
            domainUser.getUserRole(),
            domainUser.getSpecialtyId(),
            domainUser.getEntryYear(),
            domainUser.getCreatedAt());
    String token = jwtService.generateToken(jUser.getId(), jUser.getEmail(), jUser.getUserRole());

    return new AuthResponse(token, "Bearer", jwtService.getTokenLifetimeSeconds(), user);
  }
}
