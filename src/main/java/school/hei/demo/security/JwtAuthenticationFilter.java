package school.hei.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String SESSION_COOKIE = "session";

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = resolveToken(request);

    if (token != null
        && SecurityContextHolder.getContext().getAuthentication() == null
        && jwtService.isTokenValid(token)) {
      authenticate(request, token);
    }

    filterChain.doFilter(request, response);
  }

  private void authenticate(HttpServletRequest request, String token) {
    try {
      UUID userId = UUID.fromString(jwtService.extractSubject(token));
      UserDetails userDetails = userDetailsService.loadUserById(userId);
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      ((UsernamePasswordAuthenticationToken) authentication)
          .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (RuntimeException e) {
      SecurityContextHolder.clearContext();
    }
  }

  private String resolveToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
      return authorization.substring(BEARER_PREFIX.length()).trim();
    }

    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (SESSION_COOKIE.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }
}
