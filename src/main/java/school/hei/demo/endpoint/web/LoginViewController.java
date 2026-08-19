package school.hei.demo.endpoint.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import school.hei.demo.domain.dto.request.LoginRequest;
import school.hei.demo.domain.dto.response.AuthResponse;
import school.hei.demo.service.AuthService;

@Controller
@RequiredArgsConstructor
public class LoginViewController {

  private final AuthService authService;

  @GetMapping("/web/login")
  public String loginPage() {
    return "login";
  }

  @PostMapping("/web/login")
  public String login(
      @RequestParam String email, @RequestParam String password, HttpServletResponse response) {

    AuthResponse authResponse = authService.login(new LoginRequest(email, password));

    Cookie sessionCookie = new Cookie("session", authResponse.getAccessToken());

    sessionCookie.setHttpOnly(true);
    sessionCookie.setPath("/");
    sessionCookie.setMaxAge(authResponse.getExpiresIn());

    response.addCookie(sessionCookie);

    return "redirect:/web/promotions";
  }
}
