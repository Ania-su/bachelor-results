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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.request.GroupRequest;
import school.hei.demo.domain.dto.response.GroupPage;
import school.hei.demo.domain.dto.response.GroupResponse;
import school.hei.demo.domain.dto.response.PageMetadata;
import school.hei.demo.endpoint.rest.controller.GroupController;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.ConflictException;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.service.GroupService;
import school.hei.demo.service.StudentGroupHistoryService;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {
  @Mock GroupService service;
  @Mock StudentGroupHistoryService studentGroupHistoryService;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new GroupController(service, studentGroupHistoryService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void shouldCreateListWithFilterUpdateAndDelete() throws Exception {
    UUID id = UUID.randomUUID();
    GroupResponse group = new GroupResponse(id, "A1", 2025);
    when(service.create(any(GroupRequest.class))).thenReturn(group);
    when(service.list(2025, 1, 2))
        .thenReturn(new GroupPage(List.of(group), new PageMetadata(1, 2, 3, 2)));
    when(service.update(org.mockito.ArgumentMatchers.eq(id), any(GroupRequest.class)))
        .thenReturn(group);
    mockMvc
        .perform(
            post("/groups")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new GroupRequest("A1", 2025))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reference").value("A1"));
    mockMvc
        .perform(
            get("/groups").param("page", "1").param("pageSize", "2").param("academicYear", "2025"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.pageSize").value(2));
    mockMvc
        .perform(
            patch("/groups/{id}", id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new GroupRequest("A1", 2025))))
        .andExpect(status().isOk());
    mockMvc.perform(delete("/groups/{id}", id)).andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnExpectedErrors() throws Exception {
    UUID id = UUID.randomUUID();
    lenient().when(service.get(id)).thenThrow(new NotFoundException("missing"));
    lenient()
        .when(service.create(any(GroupRequest.class)))
        .thenThrow(new ConflictException("duplicate"));
    lenient().doThrow(new BadRequestException("invalid")).when(service).create((GroupRequest) null);
    mockMvc.perform(get("/groups/{id}", id)).andExpect(status().isNotFound());
    mockMvc
        .perform(post("/groups").contentType("application/json").content("null"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            post("/groups")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new GroupRequest("A1", 2025))))
        .andExpect(status().isConflict());
    lenient().when(service.list(null, -1, 20)).thenThrow(new BadRequestException("invalid page"));
    mockMvc.perform(get("/groups").param("page", "-1")).andExpect(status().isBadRequest());
  }
}
