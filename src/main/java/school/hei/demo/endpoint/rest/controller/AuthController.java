package school.hei.demo.endpoint.rest.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.demo.domain.dto.request.LoginRequest;
import school.hei.demo.domain.dto.response.AuthResponse;
import school.hei.demo.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @RequestBody LoginRequest request, HttpServletResponse response) {
    AuthResponse authResponse = authService.login(request);

    Cookie sessionCookie = new Cookie("session", authResponse.getAccessToken());
    sessionCookie.setHttpOnly(true);
    sessionCookie.setPath("/");
    sessionCookie.setMaxAge(authResponse.getExpiresIn());
    response.addCookie(sessionCookie);

    return ResponseEntity.ok(authResponse);
  }
}
