package school.hei.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import school.hei.demo.domain.dto.request.ExamRequest;
import school.hei.demo.domain.dto.response.ExamResponse;
import school.hei.demo.endpoint.rest.controller.ExamController;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.service.ExamService;

@ExtendWith(MockitoExtension.class)
class ExamControllerTest {
  @Mock ExamService service;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ExamController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void shouldCreateListGetUpdateAndDeleteExam() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    ExamResponse exam =
        new ExamResponse(
            examId, courseId, Instant.parse("2025-06-15T10:00:00Z"), BigDecimal.valueOf(0.5));
    ExamRequest request = new ExamRequest(exam.getDateExam(), exam.getCoef());
    when(service.create(eq(courseId), any(ExamRequest.class))).thenReturn(exam);
    when(service.listForCourse(courseId)).thenReturn(List.of(exam));
    when(service.get(courseId, examId)).thenReturn(exam);
    when(service.update(eq(courseId), eq(examId), any(ExamRequest.class))).thenReturn(exam);

    mockMvc
        .perform(
            post("/courses/{courseId}/exams", courseId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(examId.toString()));
    mockMvc
        .perform(get("/courses/{courseId}/exams", courseId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].coef").value(0.5));
    mockMvc
        .perform(get("/courses/{courseId}/exams/{examId}", courseId, examId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courseId").value(courseId.toString()));
    mockMvc
        .perform(
            patch("/courses/{courseId}/exams/{examId}", courseId, examId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
    mockMvc
        .perform(delete("/courses/{courseId}/exams/{examId}", courseId, examId))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnNotFoundForUnknownExam() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    when(service.get(courseId, examId)).thenThrow(new NotFoundException("missing"));

    mockMvc
        .perform(get("/courses/{courseId}/exams/{examId}", courseId, examId))
        .andExpect(status().isNotFound());
  }
}
