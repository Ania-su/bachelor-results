package school.hei.demo.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import school.hei.demo.entity.User;
import school.hei.demo.exception.UnauthorizedException;
import school.hei.demo.security.CustomUserDetails;

@Service
public class CurrentUserService {
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
      throw new UnauthorizedException("Authentication required");
    }

    return userDetails.getUser();
  }
}
