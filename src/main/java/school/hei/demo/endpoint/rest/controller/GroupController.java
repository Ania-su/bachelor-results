package school.hei.demo.endpoint.rest.controller;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupPage;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.endpoint.rest.controller.mapper.GroupRestMapper;
import school.hei.demo.service.GroupService;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
public class GroupController {

  private final GroupService service;
  private final GroupRestMapper mapper;

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
}
