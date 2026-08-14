package school.hei.demo.endpoint.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.demo.domain.dto.request.UserCreate;
import school.hei.demo.domain.dto.request.UserUpdate;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.UserPage;
import school.hei.demo.enums.UserRole;
import school.hei.demo.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping
  public UserPage findAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) UserRole role,
      @RequestParam(required = false) String reference,
      @RequestParam(required = false) String firstName,
      @RequestParam(required = false) String lastName,
      @RequestParam(required = false) String email) {
    return userService.findAll(page, pageSize, role, reference, firstName, lastName, email);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public User createUser(@RequestBody(required = false) UserCreate request) {
    return userService.create(request);
  }

  @GetMapping("/{userId}")
  public User findById(@PathVariable String userId) {
    return userService.findById(userId);
  }

  @PatchMapping("/{userId}")
  public User updateUser(
      @PathVariable String userId, @RequestBody(required = false) UserUpdate request) {
    return userService.update(userId, request);
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable String userId) {
    userService.delete(userId);
  }
}
