package school.hei.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.request.GradeCreate;
import school.hei.demo.domain.dto.request.GradeUpdate;
import school.hei.demo.domain.dto.response.GradeHistoryResponse;
import school.hei.demo.domain.dto.response.GradePage;
import school.hei.demo.domain.dto.response.GradeResponse;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.endpoint.rest.controller.GradeController;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.service.GradeHistoryService;
import school.hei.demo.service.GradeService;

@ExtendWith(MockitoExtension.class)
class GradeControllerTest {
  @Mock GradeService gradeService;
  @Mock GradeHistoryService gradeHistoryService;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private final UUID courseId = UUID.randomUUID();
  private final UUID examId = UUID.randomUUID();
  private final UUID gradeId = UUID.randomUUID();
  private final UUID studentId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new GradeController(gradeService, gradeHistoryService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void shouldHandleGradeCrudMappings() throws Exception {
    var response =
        GradeResponse.builder()
            .id(gradeId)
            .studentId(studentId)
            .examId(examId)
            .value(BigDecimal.TEN)
            .build();
    when(gradeService.list(courseId, examId, studentId, 1, 10))
        .thenReturn(new GradePage(java.util.List.of(response), new PageMetadata(1, 10, 1, 1)));
    when(gradeService.create(any(), any(), any())).thenReturn(response);
    when(gradeService.get(courseId, examId, gradeId)).thenReturn(response);
    when(gradeService.update(any(), any(), any(), any())).thenReturn(response);
    when(gradeService.delete(courseId, examId, gradeId)).thenReturn(response);

    mockMvc
        .perform(
            get("/courses/{courseId}/exams/{examId}/grades", courseId, examId)
                .param("studentId", studentId.toString())
                .param("page", "1")
                .param("pageSize", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].value").value(10));
    mockMvc
        .perform(
            post("/courses/{courseId}/exams/{examId}/grades", courseId, examId)
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(new GradeCreate(studentId, BigDecimal.TEN))))
        .andExpect(status().isCreated());
    mockMvc
        .perform(
            get("/courses/{courseId}/exams/{examId}/grades/{gradeId}", courseId, examId, gradeId))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            patch("/courses/{courseId}/exams/{examId}/grades/{gradeId}", courseId, examId, gradeId)
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(new GradeUpdate(BigDecimal.TEN, "Correction"))))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            delete(
                "/courses/{courseId}/exams/{examId}/grades/{gradeId}", courseId, examId, gradeId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(gradeId.toString()));
  }

  @Test
  void shouldReturnGradeHistoryForExam() throws Exception {
    var historyEntry =
        new GradeHistoryResponse(
            UUID.randomUUID(),
            gradeId,
            new BigDecimal("8.00"),
            new BigDecimal("14.00"),
            "Erreur de transcription",
            UUID.randomUUID(),
            Instant.parse("2026-08-18T10:00:00Z"));
    when(gradeHistoryService.listForExam(examId)).thenReturn(List.of(historyEntry));

    mockMvc
        .perform(get("/courses/{courseId}/exams/{examId}/grades/history", courseId, examId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].oldValue").value(8.00))
        .andExpect(jsonPath("$[0].newValue").value(14.00))
        .andExpect(jsonPath("$[0].reason").value("Erreur de transcription"));
  }

  @Test
  void shouldReturnEmptyListWhenNoHistoryForExam() throws Exception {
    when(gradeHistoryService.listForExam(examId)).thenReturn(List.of());

    mockMvc
        .perform(get("/courses/{courseId}/exams/{examId}/grades/history", courseId, examId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void shouldMapNotFoundTo404() throws Exception {
    when(gradeService.get(courseId, examId, gradeId)).thenThrow(new NotFoundException("missing"));
    mockMvc
        .perform(
            get("/courses/{courseId}/exams/{examId}/grades/{gradeId}", courseId, examId, gradeId))
        .andExpect(status().isNotFound());
  }
}
