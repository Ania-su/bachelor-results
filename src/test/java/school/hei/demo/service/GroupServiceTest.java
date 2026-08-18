package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupPage;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.domain.mappers.GroupMapper;
import school.hei.demo.endpoint.rest.controller.mapper.GroupRestMapper;
import school.hei.demo.entity.Group;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.GroupRepository;
import school.hei.demo.repository.model.JGroup;
import school.hei.demo.validators.GroupValidator;
import school.hei.demo.validators.PaginationValidator;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {
  @Mock GroupRepository repository;
  @Mock GroupMapper mapper;
  @Mock GroupValidator validator;
  @Mock PaginationValidator paginationValidator;
  @Mock GroupRestMapper restMapper;
  @InjectMocks GroupService service;

  @Test
  void shouldListWithYearAndPagination() {
    JGroup entity = new JGroup(UUID.randomUUID(), "A1", 2025);
    Group group = new Group(entity.getId(), "A1", 2025);
    GroupResponse response = new GroupResponse(entity.getId(), "A1", 2025);
    when(repository.findAllFiltered(eq(2025), any()))
        .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(1, 2), 3));
    when(mapper.toDomain(entity)).thenReturn(group);
    when(restMapper.toResponse(group)).thenReturn(response);

    GroupPage page = service.list(2025, 1, 2);
    assertEquals(1, page.getContent().size());
    assertEquals(3, page.getPage().getTotalElements());
    verify(paginationValidator).validate(1, 2);
  }

  @Test
  void shouldCreateUpdateAndDelete() {
    UUID id = UUID.randomUUID();
    GroupRequest request = new GroupRequest("A1", 2025);
    Group domain = new Group(null, "A1", 2025);
    Group saved = new Group(id, "B1", 2024);
    GroupResponse response = new GroupResponse(id, "B1", 2024);
    JGroup entity = new JGroup(id, "B1", 2024);
    when(restMapper.toDomain(request)).thenReturn(domain);
    when(restMapper.toResponse(any(Group.class))).thenReturn(response);
    when(repository.save(any())).thenReturn(entity);
    when(mapper.toEntity(any(Group.class))).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(saved);
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.existsById(id)).thenReturn(true);
    assertEquals(id, service.create(request).getId());
    assertEquals("B1", service.update(id, request).getReference());
    service.delete(id);
    verify(repository).deleteById(id);
  }

  @Test
  void shouldRejectMissingGroupsAndInvalidPagination() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    when(repository.existsById(id)).thenReturn(false);
    assertThrows(NotFoundException.class, () -> service.get(id));
    assertThrows(NotFoundException.class, () -> service.update(id, new GroupRequest("A1", 2025)));
    assertThrows(NotFoundException.class, () -> service.delete(id));
    doThrow(new BadRequestException("invalid page")).when(paginationValidator).validate(-1, 20);
    assertThrows(BadRequestException.class, () -> service.list(null, -1, 20));
  }
}
