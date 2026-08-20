package school.hei.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.repository.GradeHistoryRepository;
import school.hei.demo.repository.model.JGradeHistory;

@ExtendWith(MockitoExtension.class)
class GradeHistoryServiceTest {

  @Mock GradeHistoryRepository repository;

  GradeHistoryService service;

  @BeforeEach
  void setUp() {
    service = new GradeHistoryService(repository);
  }

  @Test
  void shouldRecordHistoryEntryWithGivenValues() {
    UUID gradeId = UUID.randomUUID();
    UUID changedBy = UUID.randomUUID();
    BigDecimal oldValue = new BigDecimal("8.00");
    BigDecimal newValue = new BigDecimal("14.00");
    String reason = "Erreur de transcription corrigée";

    service.record(gradeId, oldValue, newValue, reason, changedBy);

    ArgumentCaptor<JGradeHistory> captor = ArgumentCaptor.forClass(JGradeHistory.class);
    verify(repository).save(captor.capture());

    JGradeHistory saved = captor.getValue();
    assertThat(saved.getGradeId()).isEqualTo(gradeId);
    assertThat(saved.getOldValue()).isEqualByComparingTo(oldValue);
    assertThat(saved.getNewValue()).isEqualByComparingTo(newValue);
    assertThat(saved.getReason()).isEqualTo(reason);
    assertThat(saved.getChangedBy()).isEqualTo(changedBy);
    assertThat(saved.getChangedAt()).isNotNull();
  }

  @Test
  void shouldAllowNullOldValueForFirstGrade() {
    UUID gradeId = UUID.randomUUID();
    UUID changedBy = UUID.randomUUID();

    service.record(gradeId, null, new BigDecimal("12.00"), "Première saisie", changedBy);

    ArgumentCaptor<JGradeHistory> captor = ArgumentCaptor.forClass(JGradeHistory.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getOldValue()).isNull();
  }

  @Test
  void shouldRejectBlankReason() {
    UUID gradeId = UUID.randomUUID();
    UUID changedBy = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                service.record(
                    gradeId, new BigDecimal("8.00"), new BigDecimal("14.00"), "  ", changedBy))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("reason");

    verifyNoInteractions(repository);
  }

  @Test
  void shouldRejectNullReason() {
    UUID gradeId = UUID.randomUUID();
    UUID changedBy = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                service.record(
                    gradeId, new BigDecimal("8.00"), new BigDecimal("14.00"), null, changedBy))
        .isInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(repository);
  }

  @Test
  void shouldListHistoryMappedToResponseOrderedByRepository() {
    UUID examId = UUID.randomUUID();
    UUID gradeId = UUID.randomUUID();
    UUID changedBy = UUID.randomUUID();
    Instant changedAt = Instant.parse("2026-08-18T10:00:00Z");

    JGradeHistory entity =
        JGradeHistory.builder()
            .id(UUID.randomUUID())
            .gradeId(gradeId)
            .oldValue(new BigDecimal("8.00"))
            .newValue(new BigDecimal("14.00"))
            .reason("Réclamation acceptée")
            .changedBy(changedBy)
            .changedAt(changedAt)
            .build();

    when(repository.findAllByExamId(examId)).thenReturn(List.of(entity));

    var result = service.listForExam(examId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getGradeId()).isEqualTo(gradeId);
    assertThat(result.get(0).getOldValue()).isEqualByComparingTo("8.00");
    assertThat(result.get(0).getNewValue()).isEqualByComparingTo("14.00");
    assertThat(result.get(0).getReason()).isEqualTo("Réclamation acceptée");
    assertThat(result.get(0).getChangedAt()).isEqualTo(changedAt);
  }

  @Test
  void shouldReturnEmptyListWhenNoHistoryForExam() {
    UUID examId = UUID.randomUUID();
    when(repository.findAllByExamId(examId)).thenReturn(List.of());

    var result = service.listForExam(examId);

    assertThat(result).isEmpty();
  }
}
