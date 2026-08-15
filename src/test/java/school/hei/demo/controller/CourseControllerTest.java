package school.hei.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.request.CourseRequest;
import school.hei.demo.endpoint.rest.controller.CourseController;
import school.hei.demo.endpoint.rest.controller.mapper.CourseRestMapper;
import school.hei.demo.entity.Course;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.ConflictException;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.service.CourseService;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {
  @Mock CourseService service;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new CourseController(service, new CourseRestMapper()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void shouldCreateListWithFiltersUpdateAndDelete() throws Exception {
    UUID id = UUID.randomUUID();
    Course course = new Course(id, "CS", "Algorithms", 2, 6);
    CourseRequest request = new CourseRequest("CS", "Algorithms", 2, 6);
    when(service.create(any())).thenReturn(course);
    when(service.list(2, "algo", 1, 2))
        .thenReturn(new PageImpl<>(List.of(course), PageRequest.of(1, 2), 3));
    when(service.update(org.mockito.ArgumentMatchers.eq(id), any())).thenReturn(course);
    mockMvc
        .perform(
            post("/courses")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Algorithms"));
    mockMvc
        .perform(
            get("/courses")
                .param("page", "1")
                .param("pageSize", "2")
                .param("semester", "2")
                .param("search", "algo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.pageSize").value(2));
    mockMvc
        .perform(
            patch("/courses/{id}", id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
    mockMvc.perform(delete("/courses/{id}", id)).andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnExpectedErrors() throws Exception {
    UUID id = UUID.randomUUID();
    lenient().when(service.get(id)).thenThrow(new NotFoundException("missing"));
    lenient().when(service.create(any(Course.class))).thenThrow(new ConflictException("duplicate"));
    lenient().doThrow(new BadRequestException("invalid")).when(service).create((Course) null);
    CourseRequest request = new CourseRequest("CS", "Algorithms", 2, 6);
    mockMvc.perform(get("/courses/{id}", id)).andExpect(status().isNotFound());
    mockMvc
        .perform(post("/courses").contentType("application/json").content("null"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            post("/courses")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
    lenient()
        .when(service.list(null, null, -1, 20))
        .thenThrow(new BadRequestException("invalid page"));
    mockMvc.perform(get("/courses").param("page", "-1")).andExpect(status().isBadRequest());
  }
}
