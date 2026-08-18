package school.hei.demo.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.response.CourseResponse;
import school.hei.demo.domain.dto.response.StudentCourseGradeResponse;
import school.hei.demo.endpoint.rest.controller.StudentCourseGradeController;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.service.StudentCourseGradeService;

@ExtendWith(MockitoExtension.class)
class StudentCourseGradeControllerTest {
  @Mock StudentCourseGradeService service;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new StudentCourseGradeController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void getStudentCourseGrade_returnsResponse() throws Exception {
    UUID courseId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    StudentCourseGradeResponse response =
        new StudentCourseGradeResponse(
            null, new CourseResponse(courseId, "REF", "Title", 1, 5), java.util.List.of(), 20.0);
    when(service.get(courseId, studentId)).thenReturn(response);

    mockMvc
        .perform(get("/courses/{courseId}/student-grade/{studentId}", courseId, studentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.average").value(20.0))
        .andExpect(jsonPath("$.course.reference").value("REF"));
  }
}
