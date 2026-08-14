package school.hei.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.domain.dto.response.UserPage;
import school.hei.demo.endpoint.rest.controller.UserController;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.ConflictException;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
  @Mock UserService userService;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new UserController(userService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void shouldCreateUser() throws Exception {
    UserCreate request = request();
    when(userService.create(any(UserCreate.class))).thenReturn(responseUser());

    mockMvc
        .perform(
            post("/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(request.email()))
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void shouldFindUsersWithDefaultParameters() throws Exception {
    when(userService.findAll(0, 20, null, null, null, null, null))
        .thenReturn(new UserPage(java.util.List.of(responseUser()), new PageMetadata(0, 20, 1, 1)));

    mockMvc
        .perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page.pageSize").value(20));
  }

  @Test
  void shouldPassUserFiltersToService() throws Exception {
    when(userService.findAll(2, 5, UserRole.TEACHER, "ref", "john", "doe", "example"))
        .thenReturn(new UserPage(java.util.List.of(), new PageMetadata(2, 5, 0, 0)));

    mockMvc
        .perform(
            get("/users")
                .param("page", "2")
                .param("pageSize", "5")
                .param("role", "TEACHER")
                .param("reference", "ref")
                .param("firstName", "john")
                .param("lastName", "doe")
                .param("email", "example"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.page").value(2));
  }

  @Test
  void shouldReturnBadRequestForInvalidPage() throws Exception {
    when(userService.findAll(-1, 20, null, null, null, null, null))
        .thenThrow(new BadRequestException("page must be greater than or equal to 0"));

    mockMvc
        .perform(get("/users").param("page", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void shouldReturnBadRequestForNullRequest() throws Exception {
    doThrow(new BadRequestException("Request body is required"))
        .when(userService)
        .create((UserCreate) null);

    mockMvc
        .perform(post("/users").contentType("application/json").content("null"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Request body is required"));
  }

  @Test
  void shouldReturnConflictForDuplicateUser() throws Exception {
    when(userService.create(any(UserCreate.class)))
        .thenThrow(new ConflictException("A user with this reference already exists"));

    mockMvc
        .perform(
            post("/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void shouldReturnBadRequestForMalformedJson() throws Exception {
    mockMvc
        .perform(post("/users").contentType("application/json").content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  private UserCreate request() {
    return new UserCreate(
        "REF001", "John", "Doe", "john@example.com", "password", UserRole.STUDENT,
        UUID.randomUUID(), 2025);
  }

  private User responseUser() {
    return new User(
        UUID.randomUUID(), "REF001", "John", "Doe", "john@example.com", UserRole.STUDENT,
        UUID.randomUUID(), 2025, null);
  }
}
