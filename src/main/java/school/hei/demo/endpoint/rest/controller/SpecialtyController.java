package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.endpoint.rest.controller.mapper.SpecialtyRestMapper;
import school.hei.demo.service.SpecialtyService;

@RestController
@RequestMapping("/specialties")
@AllArgsConstructor
public class SpecialtyController {

  private final SpecialtyService service;
  private final SpecialtyRestMapper mapper;

  @GetMapping
  public ResponseEntity<List<SpecialtyResponse>> listSpecialties() {
    var body = service.list().stream().map(mapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{specialtyId}")
  public ResponseEntity<SpecialtyResponse> getSpecialty(@PathVariable UUID specialtyId) {
    return ResponseEntity.ok(mapper.toResponse(service.get(specialtyId)));
  }

  @PostMapping
  public ResponseEntity<SpecialtyResponse> createSpecialty(@RequestBody SpecialtyRequest request) {
    var created = mapper.toResponse(service.create(mapper.toDomain(request)));
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PatchMapping("/{specialtyId}")
  public ResponseEntity<SpecialtyResponse> updateSpecialty(
      @PathVariable UUID specialtyId, @RequestBody SpecialtyRequest request) {
    var updated = mapper.toResponse(service.update(specialtyId, mapper.toDomain(request)));
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{specialtyId}")
  public ResponseEntity<Void> deleteSpecialty(@PathVariable UUID specialtyId) {
    service.delete(specialtyId);
    return ResponseEntity.noContent().build();
  }
}
