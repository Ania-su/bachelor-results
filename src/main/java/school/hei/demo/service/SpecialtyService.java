package school.hei.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.mappers.SpecialtyMapper;
import school.hei.demo.entity.Specialty;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.validators.SpecialtyValidator;

@Service
@AllArgsConstructor
public class SpecialtyService {

  private final SpecialtyRepository repository;
  private final SpecialtyMapper mapper;
  private final SpecialtyValidator validator;

  public List<Specialty> list() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  public Specialty get(UUID id) {
    return repository
        .findById(id)
        .map(mapper::toDomain)
        .orElseThrow(() -> new NotFoundException("Specialty " + id + " not found"));
  }

  public Specialty create(Specialty specialty) {
    validator.validate(specialty);
    return mapper.toDomain(repository.save(mapper.toEntity(specialty)));
  }

  public Specialty update(UUID id, Specialty updated) {
    validator.validate(updated);
    var existing =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Specialty " + id + " not found"));
    existing.setCode(updated.getCode());
    existing.setLabel(updated.getLabel());
    return mapper.toDomain(repository.save(existing));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("Specialty " + id + " not found");
    }
    repository.deleteById(id);
  }
}
