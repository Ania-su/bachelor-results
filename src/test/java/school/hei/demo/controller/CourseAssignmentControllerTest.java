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
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.request.CourseAssignmentRequest;
import school.hei.demo.domain.dto.response.CourseAssignmentResponse;
import school.hei.demo.endpoint.rest.controller.CourseAssignmentController;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.service.CourseAssignmentService;

@ExtendWith(MockitoExtension.class)
class CourseAssignmentControllerTest {
  @Mock CourseAssignmentService service;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new CourseAssignmentController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void shouldCreateListGetUpdateAndDeleteAssignment() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    CourseAssignmentResponse assignment =
        new CourseAssignmentResponse(assignmentId, courseId, teacherId, groupId);
    CourseAssignmentRequest request = new CourseAssignmentRequest(teacherId, groupId);
    when(service.create(eq(courseId), any(CourseAssignmentRequest.class))).thenReturn(assignment);
    when(service.listForCourse(courseId)).thenReturn(List.of(assignment));
    when(service.get(courseId, assignmentId)).thenReturn(assignment);
    when(service.update(eq(courseId), eq(assignmentId), any(CourseAssignmentRequest.class)))
        .thenReturn(assignment);

    mockMvc
        .perform(
            post("/courses/{courseId}/assignments", courseId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(assignmentId.toString()));
    mockMvc
        .perform(get("/courses/{courseId}/assignments", courseId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].teacherId").value(teacherId.toString()));
    mockMvc
        .perform(get("/courses/{courseId}/assignments/{assignmentId}", courseId, assignmentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groupId").value(groupId.toString()));
    mockMvc
        .perform(
            patch("/courses/{courseId}/assignments/{assignmentId}", courseId, assignmentId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
    mockMvc
        .perform(delete("/courses/{courseId}/assignments/{assignmentId}", courseId, assignmentId))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnNotFoundForUnknownAssignment() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();
    when(service.get(courseId, assignmentId)).thenThrow(new NotFoundException("missing"));

    mockMvc
        .perform(get("/courses/{courseId}/assignments/{assignmentId}", courseId, assignmentId))
        .andExpect(status().isNotFound());
  }
}
