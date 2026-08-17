package school.hei.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.mappers.ExamGroupMapper;
import school.hei.demo.entity.ExamGroup;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.ExamGroupRepository;
import school.hei.demo.validators.ExamGroupValidator;

@Service
@AllArgsConstructor
public class ExamGroupService {

  private final ExamGroupRepository repository;
  private final ExamGroupMapper mapper;
  private final ExamGroupValidator validator;
  private final ExamService examService;

  public List<ExamGroup> listForExam(UUID courseId, UUID examId) {
    examService.get(courseId, examId);
    return repository.findAllByExam_Id(examId).stream().map(mapper::toDomain).toList();
  }

  public ExamGroup get(UUID courseId, UUID examId, UUID examGroupId) {
    examService.get(courseId, examId);
    var examGroup =
        repository
            .findById(examGroupId)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("ExamGroup " + examGroupId + " not found"));
    ensureBelongsToExam(examGroup, examId);
    return examGroup;
  }

  public ExamGroup create(UUID courseId, UUID examId, ExamGroup examGroup) {
    examService.get(courseId, examId);
    examGroup.setExamId(examId);
    validator.validate(examGroup);
    return mapper.toDomain(repository.save(mapper.toEntity(examGroup)));
  }

  public void delete(UUID courseId, UUID examId, UUID examGroupId) {
    var examGroup = get(courseId, examId, examGroupId);
    repository.deleteById(examGroup.getId());
  }

  private void ensureBelongsToExam(ExamGroup examGroup, UUID examId) {
    if (!examGroup.getExamId().equals(examId)) {
      throw new NotFoundException("ExamGroup not found for this exam");
    }
  }
}
