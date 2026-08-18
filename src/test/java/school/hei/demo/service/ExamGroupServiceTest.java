package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import school.hei.demo.domain.dto.request.ExamGroupRequest;
import school.hei.demo.domain.dto.response.ExamGroupResponse;
import school.hei.demo.domain.dto.response.ExamResponse;
import school.hei.demo.domain.mappers.ExamGroupMapper;
import school.hei.demo.entity.ExamGroup;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.ExamGroupRepository;
import school.hei.demo.repository.model.JExamGroup;
import school.hei.demo.validators.ExamGroupValidator;

@ExtendWith(MockitoExtension.class)
class ExamGroupServiceTest {
  @Mock ExamGroupRepository repository;
  @Mock ExamGroupMapper mapper;
  @Mock ExamGroupValidator validator;
  @Mock ExamService examService;
  @InjectMocks ExamGroupService service;

  private ExamGroup domain(UUID examId, UUID groupId) {
    return ExamGroup.builder().id(UUID.randomUUID()).examId(examId).groupId(groupId).build();
  }

  @Test
  void listForExam_delegatesAfterOwnershipCheck() {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    ExamGroup eg = domain(examId, groupId);
    JExamGroup j = JExamGroup.builder().build();
    ExamGroupResponse response = new ExamGroupResponse(eg.getId(), examId, groupId);
    when(examService.get(courseId, examId))
        .thenReturn(new ExamResponse(examId, courseId, null, null));
    when(repository.findAllByExam_Id(examId)).thenReturn(List.of(j));
    when(mapper.toDomain(j)).thenReturn(eg);
    when(mapper.toResponse(eg)).thenReturn(response);

    assertEquals(List.of(response), service.listForExam(courseId, examId));
    verify(examService).get(courseId, examId);
  }

  @Test
  void get_returnsResponseWhenOwnedByExam() {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID examGroupId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    ExamGroup eg = domain(examId, groupId);
    JExamGroup j = JExamGroup.builder().build();
    ExamGroupResponse response = new ExamGroupResponse(eg.getId(), examId, groupId);
    when(examService.get(courseId, examId))
        .thenReturn(new ExamResponse(examId, courseId, null, null));
    when(repository.findById(examGroupId)).thenReturn(Optional.of(j));
    when(mapper.toDomain(j)).thenReturn(eg);
    when(mapper.toResponse(eg)).thenReturn(response);

    assertEquals(response, service.get(courseId, examId, examGroupId));
  }

  @Test
  void get_throwsWhenNotFound() {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID examGroupId = UUID.randomUUID();
    when(examService.get(courseId, examId))
        .thenReturn(new ExamResponse(examId, courseId, null, null));
    when(repository.findById(examGroupId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.get(courseId, examId, examGroupId));
  }

  @Test
  void get_throwsWhenBelongingToAnotherExam() {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID otherExamId = UUID.randomUUID();
    UUID examGroupId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    ExamGroup eg = domain(otherExamId, groupId);
    JExamGroup j = JExamGroup.builder().build();
    when(examService.get(courseId, examId))
        .thenReturn(new ExamResponse(examId, courseId, null, null));
    when(repository.findById(examGroupId)).thenReturn(Optional.of(j));
    when(mapper.toDomain(j)).thenReturn(eg);

    assertThrows(NotFoundException.class, () -> service.get(courseId, examId, examGroupId));
  }

  @Test
  void create_validatesAndPersists() {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    ExamGroupRequest request = new ExamGroupRequest(groupId);
    ExamGroup eg = domain(examId, groupId);
    JExamGroup j = JExamGroup.builder().build();
    ExamGroupResponse response = new ExamGroupResponse(eg.getId(), examId, groupId);
    when(examService.get(courseId, examId))
        .thenReturn(new ExamResponse(examId, courseId, null, null));
    when(mapper.toDomain(request)).thenReturn(eg);
    when(mapper.toEntity(eg)).thenReturn(j);
    when(repository.save(j)).thenReturn(j);
    when(mapper.toDomain(j)).thenReturn(eg);
    when(mapper.toResponse(eg)).thenReturn(response);

    assertEquals(response, service.create(courseId, examId, request));
    verify(validator).validate(eg);
    assertEquals(examId, eg.getExamId());
  }

  @Test
  void delete_removesExistingGroup() {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID examGroupId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    ExamGroup eg = domain(examId, groupId);
    JExamGroup j = JExamGroup.builder().build();
    when(examService.get(courseId, examId))
        .thenReturn(new ExamResponse(examId, courseId, null, null));
    when(repository.findById(examGroupId)).thenReturn(Optional.of(j));
    when(mapper.toDomain(j)).thenReturn(eg);
    when(mapper.toResponse(eg)).thenReturn(new ExamGroupResponse(eg.getId(), examId, groupId));

    service.delete(courseId, examId, examGroupId);
    verify(repository).deleteById(eg.getId());
  }
}
