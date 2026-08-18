package school.hei.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.request.ExamGroupRequest;
import school.hei.demo.domain.dto.response.ExamGroupResponse;
import school.hei.demo.endpoint.rest.controller.ExamGroupController;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.service.ExamGroupService;

@ExtendWith(MockitoExtension.class)
class ExamGroupControllerTest {
  @Mock ExamGroupService service;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ExamGroupController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void listExamGroups_returnsList() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    ExamGroupResponse response = new ExamGroupResponse(UUID.randomUUID(), examId, groupId);
    when(service.listForExam(courseId, examId)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/courses/{courseId}/exams/{examId}/groups", courseId, examId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].groupId").value(groupId.toString()));
  }

  @Test
  void getExamGroup_returnsOne() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID examGroupId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    ExamGroupResponse response = new ExamGroupResponse(examGroupId, examId, groupId);
    when(service.get(courseId, examId, examGroupId)).thenReturn(response);

    mockMvc
        .perform(
            get(
                "/courses/{courseId}/exams/{examId}/groups/{examGroupId}",
                courseId,
                examId,
                examGroupId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(examGroupId.toString()));
  }

  @Test
  void createExamGroup_returnsCreated() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    ExamGroupResponse response = new ExamGroupResponse(UUID.randomUUID(), examId, groupId);
    when(service.create(eq(courseId), eq(examId), any(ExamGroupRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/courses/{courseId}/exams/{examId}/groups", courseId, examId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new ExamGroupRequest(groupId))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.groupId").value(groupId.toString()));
  }

  @Test
  void deleteExamGroup_returnsNoContent() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID examGroupId = UUID.randomUUID();

    mockMvc
        .perform(
            delete(
                "/courses/{courseId}/exams/{examId}/groups/{examGroupId}",
                courseId,
                examId,
                examGroupId))
        .andExpect(status().isNoContent());
  }

  @Test
  void getExamGroup_notFoundReturns404() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    UUID examGroupId = UUID.randomUUID();
    when(service.get(courseId, examId, examGroupId)).thenThrow(new NotFoundException("missing"));

    mockMvc
        .perform(
            get(
                "/courses/{courseId}/exams/{examId}/groups/{examGroupId}",
                courseId,
                examId,
                examGroupId))
        .andExpect(status().isNotFound());
  }
}
