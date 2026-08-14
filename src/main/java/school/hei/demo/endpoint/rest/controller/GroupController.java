package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.endpoint.rest.controller.mapper.GroupRestMapper;
import school.hei.demo.service.GroupService;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
public class GroupController {

    private final GroupService service;
    private final GroupRestMapper mapper;

    @GetMapping
    public ResponseEntity<List<GroupResponse>> listGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer academicYear) {

        var result = service.list(academicYear, page, pageSize);
        var body = result.getContent().stream().map(mapper::toResponse).toList();

        var headers = new HttpHeaders();
        headers.add("X-Page", String.valueOf(page));
        headers.add("X-Page-Size", String.valueOf(pageSize));
        headers.add("X-Total-Elements", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));

        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(mapper.toResponse(service.get(groupId)));
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest request) {
        var created = mapper.toResponse(service.create(mapper.toDomain(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable UUID groupId,
            @RequestBody GroupRequest request) {
        var updated = mapper.toResponse(service.update(groupId, mapper.toDomain(request)));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId) {
        service.delete(groupId);
        return ResponseEntity.noContent().build();
    }
}
