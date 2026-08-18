package school.hei.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupPage;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.domain.mappers.GroupMapper;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.validators.GroupValidator;
import school.hei.demo.validators.PaginationValidator;

@Service
@AllArgsConstructor
public class GroupService {

  private final GroupRepository repository;
  private final GroupMapper mapper;
  private final GroupValidator validator;
  private final PaginationValidator paginationValidator;

  public List<GroupResponse> listAll() {
    return repository.findAll().stream().map(mapper::toDomain).map(mapper::toResponse).toList();
  }

  public GroupPage list(Integer academicYear, int page, int pageSize) {
    paginationValidator.validate(page, pageSize);
    var result =
        repository
            .findAllFiltered(academicYear, PageRequest.of(page, pageSize))
            .map(mapper::toDomain);
    var body = result.getContent().stream().map(mapper::toResponse).toList();
    return new GroupPage(
        body,
        new PageMetadata(
            result.getNumber(),
            result.getSize(),
            Math.toIntExact(result.getTotalElements()),
            result.getTotalPages()));
  }

  public GroupResponse get(UUID id) {
    return mapper.toResponse(
        repository
            .findById(id)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Group " + id + " not found")));
  }

  public GroupResponse create(GroupRequest request) {
    var group = mapper.toDomain(request);
    validator.validate(group);
    return mapper.toResponse(mapper.toDomain(repository.save(mapper.toEntity(group))));
  }

  public GroupResponse update(UUID id, GroupRequest request) {
    var updated = mapper.toDomain(request);
    validator.validate(updated);
    var existing =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Group " + id + " not found"));
    existing.setReference(updated.getReference());
    existing.setAcademicYear(updated.getAcademicYear());
    return mapper.toResponse(mapper.toDomain(repository.save(existing)));
  }

  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("Group " + id + " not found");
    }
    repository.deleteById(id);
  }
}
