package school.hei.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.request.LoginRequest;
import school.hei.demo.domain.dto.response.AuthResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.endpoint.rest.controller.AuthController;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
  @Mock AuthService authService;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new AuthController(authService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void login_returnsAuthResponseAndCookie() throws Exception {
    AuthResponse response = new AuthResponse();
    response.setAccessToken("token-abc");
    response.setTokenType("Bearer");
    response.setExpiresIn(3600);
    response.setUser(
        new User(
            java.util.UUID.randomUUID(),
            "REF",
            "Jean",
            "Rakoto",
            "user@hei.mg",
            UserRole.ADMIN,
            java.util.UUID.randomUUID(),
            2024,
            java.time.Instant.now()));
    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new LoginRequest("user@hei.mg", "pw"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("token-abc"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }
}
