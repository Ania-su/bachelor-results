package school.hei.demo.endpoint.rest.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupPage;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.endpoint.rest.controller.mapper.GroupRestMapper;
import school.hei.demo.repository.model.JUser;
import school.hei.demo.service.GroupService;
import school.hei.demo.service.StudentGroupHistoryService;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
public class GroupController {

  private final GroupService service;
  private final GroupRestMapper mapper;
  private final StudentGroupHistoryService studentGroupHistoryService;

  @GetMapping
  public GroupPage listGroups(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Integer academicYear) {

    var result = service.list(academicYear, page, pageSize);
    var body = result.getContent().stream().map(mapper::toResponse).toList();
    return new GroupPage(
        body,
        new PageMetadata(
            result.getNumber(),
            result.getSize(),
            Math.toIntExact(result.getTotalElements()),
            result.getTotalPages()));
  }

  @GetMapping("/{groupId}")
  public GroupResponse getGroup(@PathVariable UUID groupId) {
    return mapper.toResponse(service.get(groupId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GroupResponse createGroup(@RequestBody GroupRequest request) {
    return mapper.toResponse(service.create(mapper.toDomain(request)));
  }

  @PatchMapping("/{groupId}")
  public GroupResponse updateGroup(@PathVariable UUID groupId, @RequestBody GroupRequest request) {
    return mapper.toResponse(service.update(groupId, mapper.toDomain(request)));
  }

  @DeleteMapping("/{groupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGroup(@PathVariable UUID groupId) {
    service.delete(groupId);
  }

  @GetMapping("/{groupId}/students")
  public List<User> listStudentsForGroup(@PathVariable UUID groupId) {
    var body =
        studentGroupHistoryService.listCurrentStudents(groupId).stream()
            .map(this::toUserResponse)
            .toList();
    return body;
  }

  @PostMapping("/{groupId}/students/{studentId}")
  @ResponseStatus(HttpStatus.CREATED)
  public void enrollStudent(
      @PathVariable UUID groupId,
      @PathVariable UUID studentId,
      @RequestParam(required = false) LocalDate startDate) {
    studentGroupHistoryService.enroll(
        studentId, groupId, startDate != null ? startDate : LocalDate.now());
  }

  @DeleteMapping("/{groupId}/students/{studentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unenrollStudent(
      @PathVariable UUID groupId,
      @PathVariable UUID studentId,
      @RequestParam(required = false) LocalDate endDate) {
    studentGroupHistoryService.unenroll(
        groupId, studentId, endDate != null ? endDate : LocalDate.now());
  }

  private User toUserResponse(JUser u) {
    return new User(
        u.getId(),
        u.getReference(),
        u.getFirstName(),
        u.getLastName(),
        u.getEmail(),
        u.getUserRole(),
        u.getSpecialtyId(),
        u.getEntryYear(),
        u.getCreatedAt());
  }
}
