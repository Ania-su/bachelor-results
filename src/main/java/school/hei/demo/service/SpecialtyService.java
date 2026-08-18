package school.hei.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.domain.mappers.SpecialtyMapper;
import school.hei.demo.endpoint.rest.controller.mapper.SpecialtyRestMapper;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.validators.SpecialtyValidator;

@Service
@AllArgsConstructor
public class SpecialtyService {

  private final SpecialtyRepository repository;
  private final SpecialtyMapper mapper;
  private final SpecialtyValidator validator;
  private final SpecialtyRestMapper restMapper;

  public List<SpecialtyResponse> list() {
    return repository.findAll().stream().map(mapper::toDomain).map(restMapper::toResponse).toList();
  }

  public SpecialtyResponse get(UUID id) {
    return restMapper.toResponse(
        repository
            .findById(id)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Specialty " + id + " not found")));
  }

  public SpecialtyResponse create(SpecialtyRequest request) {
    var specialty = restMapper.toDomain(request);
    validator.validate(specialty);
    return restMapper.toResponse(mapper.toDomain(repository.save(mapper.toEntity(specialty))));
  }

  public SpecialtyResponse update(UUID id, SpecialtyRequest request) {
    var updated = restMapper.toDomain(request);
    validator.validate(updated);
    var existing =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Specialty " + id + " not found"));
    existing.setCode(updated.getCode());
    existing.setLabel(updated.getLabel());
    return restMapper.toResponse(mapper.toDomain(repository.save(existing)));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("Specialty " + id + " not found");
    }
    repository.deleteById(id);
  }
}
