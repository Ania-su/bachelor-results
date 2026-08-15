package school.hei.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.mappers.ExamMapper;
import school.hei.demo.entity.Exam;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.repository.ExamRepository;
import school.hei.demo.validators.ExamValidator;

@Service
@AllArgsConstructor
public class ExamService {

  private final ExamRepository repository;
  private final ExamMapper mapper;
  private final ExamValidator validator;

  public List<Exam> listForCourse(UUID courseId) {
    return repository.findAllByCourse_Id(courseId).stream().map(mapper::toDomain).toList();
  }

  public Exam get(UUID courseId, UUID examId) {
    var exam =
        repository
            .findById(examId)
            .map(mapper::toDomain)
            .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));
    ensureBelongsToCourse(exam, courseId);
    return exam;
  }

  public Exam create(UUID courseId, Exam exam) {
    exam.setCourseId(courseId);
    validator.validate(exam);
    return mapper.toDomain(repository.save(mapper.toEntity(exam)));
  }

  public Exam update(UUID courseId, UUID examId, Exam updated) {
    updated.setCourseId(courseId);
    validator.validate(updated);
    var existing =
        repository
            .findById(examId)
            .orElseThrow(() -> new NotFoundException("Exam " + examId + " not found"));
    ensureBelongsToCourse(mapper.toDomain(existing), courseId);

    var toSave = mapper.toEntity(updated.toBuilder().id(examId).build());
    return mapper.toDomain(repository.save(toSave));
  }

  public void delete(UUID courseId, UUID examId) {
    var exam = get(courseId, examId);
    repository.deleteById(exam.getId());
  }

  private void ensureBelongsToCourse(Exam exam, UUID courseId) {
    if (!exam.getCourseId().equals(courseId)) {
      throw new NotFoundException("Exam not found for this course");
    }
  }
}
