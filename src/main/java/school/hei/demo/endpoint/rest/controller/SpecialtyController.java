package school.hei.demo.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.service.SpecialtyService;

@RestController
@RequestMapping("/specialties")
@AllArgsConstructor
public class SpecialtyController {

  private final SpecialtyService service;

  @GetMapping
  public List<SpecialtyResponse> listSpecialties() {
    return service.list();
  }

  @GetMapping("/{specialtyId}")
  public SpecialtyResponse getSpecialty(@PathVariable UUID specialtyId) {
    return service.get(specialtyId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SpecialtyResponse createSpecialty(@RequestBody SpecialtyRequest request) {
    return service.create(request);
  }

  @PatchMapping("/{specialtyId}")
  public SpecialtyResponse updateSpecialty(
      @PathVariable UUID specialtyId, @RequestBody SpecialtyRequest request) {
    return service.update(specialtyId, request);
  }

  @DeleteMapping("/{specialtyId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSpecialty(@PathVariable UUID specialtyId) {
    service.delete(specialtyId);
  }
}
