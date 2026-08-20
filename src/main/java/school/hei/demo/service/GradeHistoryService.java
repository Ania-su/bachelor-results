package school.hei.demo.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.demo.domain.dto.response.GradeHistoryResponse;
import school.hei.demo.repository.GradeHistoryRepository;
import school.hei.demo.repository.model.JGradeHistory;

@Service
@AllArgsConstructor
public class GradeHistoryService {

  private final GradeHistoryRepository repository;

  public void record(
      UUID gradeId,
      java.math.BigDecimal oldValue,
      java.math.BigDecimal newValue,
      String reason,
      UUID changedBy) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("reason is required when changing a grade");
    }
    repository.save(
        JGradeHistory.builder()
            .gradeId(gradeId)
            .oldValue(oldValue)
            .newValue(newValue)
            .reason(reason)
            .changedBy(changedBy)
            .changedAt(Instant.now())
            .build());
  }

  public List<GradeHistoryResponse> listForExam(UUID examId) {
    return repository.findAllByExamId(examId).stream().map(this::toResponse).toList();
  }

  private GradeHistoryResponse toResponse(JGradeHistory h) {
    return new GradeHistoryResponse(
        h.getId(),
        h.getGradeId(),
        h.getOldValue(),
        h.getNewValue(),
        h.getReason(),
        h.getChangedBy(),
        h.getChangedAt());
  }
}
