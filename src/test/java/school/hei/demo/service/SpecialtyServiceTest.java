package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.domain.mappers.SpecialtyMapper;
import school.hei.demo.entity.Specialty;
import school.hei.demo.enums.CodeType;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.SpecialtyRepository;
import school.hei.demo.repository.model.JSpecialty;
import school.hei.demo.validators.SpecialtyValidator;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {
  @Mock SpecialtyRepository repository;
  @Mock SpecialtyMapper mapper;
  @Mock SpecialtyValidator validator;
  @InjectMocks SpecialtyService service;

  @Test
  void shouldListCreateUpdateAndDelete() {
    UUID id = UUID.randomUUID();
    JSpecialty entity = new JSpecialty(id, CodeType.EL, "Software");
    SpecialtyRequest request = new SpecialtyRequest(CodeType.EL, "Software");
    Specialty domain = new Specialty(null, CodeType.EL, "Software");
    Specialty saved = new Specialty(id, CodeType.EL, "Software");
    SpecialtyResponse response = new SpecialtyResponse(id, CodeType.EL, "Software");
    when(repository.findAll()).thenReturn(List.of(entity));
    when(repository.save(any())).thenReturn(entity);
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.existsById(id)).thenReturn(true);
    when(mapper.toEntity(any())).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(saved);
    when(mapper.toDomain(request)).thenReturn(domain);
    when(mapper.toResponse(any(Specialty.class))).thenReturn(response);
    assertEquals(1, service.list().size());
    assertEquals(id, service.create(request).getId());
    assertEquals("Software", service.update(id, request).getLabel());
    service.delete(id);
    verify(repository).deleteById(id);
  }

  @Test
  void shouldRejectMissingSpecialties() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    when(repository.existsById(id)).thenReturn(false);
    assertThrows(NotFoundException.class, () -> service.get(id));
    assertThrows(
        NotFoundException.class,
        () -> service.update(id, new SpecialtyRequest(CodeType.EL, "Software")));
    assertThrows(NotFoundException.class, () -> service.delete(id));
  }
}
