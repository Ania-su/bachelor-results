package school.hei.demo.service;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.mappers.GroupMapper;
import school.hei.demo.entity.Group;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.validators.GroupValidator;

@Service
@AllArgsConstructor
public class GroupService {

  private final GroupRepository repository;
  private final GroupMapper mapper;
  private final GroupValidator validator;

  public Page<Group> list(Integer academicYear, int page, int pageSize) {
    return repository
        .findAllFiltered(academicYear, PageRequest.of(page, pageSize))
        .map(mapper::toDomain);
  }

  public Group get(UUID id) {
    return repository
        .findById(id)
        .map(mapper::toDomain)
        .orElseThrow(() -> new NotFoundException("Group " + id + " not found"));
  }

  public Group create(Group group) {
    validator.validate(group);
    return mapper.toDomain(repository.save(mapper.toEntity(group)));
  }

  public Group update(UUID id, Group updated) {
    validator.validate(updated);
    var existing =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Group " + id + " not found"));
    existing.setReference(updated.getReference());
    existing.setAcademicYear(updated.getAcademicYear());
    return mapper.toDomain(repository.save(existing));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("Group " + id + " not found");
    }
    repository.deleteById(id);
  }
}
