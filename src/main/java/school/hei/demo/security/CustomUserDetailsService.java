package school.hei.demo.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.repository.UserRepository;
import school.hei.demo.repository.model.JUser;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    JUser user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new CustomUserDetails(userMapper.toDomain(user));
  }

  public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException {
    JUser user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new CustomUserDetails(userMapper.toDomain(user));
  }
}
