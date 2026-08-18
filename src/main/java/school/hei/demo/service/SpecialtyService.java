package school.hei.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.domain.mappers.SpecialtyMapper;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.validators.SpecialtyValidator;

@Service
@AllArgsConstructor
public class SpecialtyService {

  private final SpecialtyRepository repository;
  private final SpecialtyMapper mapper;
  private final SpecialtyValidator validator;

  public List<SpecialtyResponse> list() {
    return repository.findAll().stream().map(mapper::toDomain).map(mapper::toResponse).toList();
  }

  public SpecialtyResponse get(UUID id) {
    return mapper.toResponse(
        repository
            .findById(id)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Specialty " + id + " not found")));
  }

  public SpecialtyResponse create(SpecialtyRequest request) {
    var specialty = mapper.toDomain(request);
    validator.validate(specialty);
    return mapper.toResponse(mapper.toDomain(repository.save(mapper.toEntity(specialty))));
  }

  public SpecialtyResponse update(UUID id, SpecialtyRequest request) {
    var updated = mapper.toDomain(request);
    validator.validate(updated);
    var existing =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Specialty " + id + " not found"));
    existing.setCode(updated.getCode());
    existing.setLabel(updated.getLabel());
    return mapper.toResponse(mapper.toDomain(repository.save(existing)));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("Specialty " + id + " not found");
    }
    repository.deleteById(id);
  }
}
