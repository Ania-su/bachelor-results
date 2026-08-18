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
import school.hei.demo.domain.dto.request.SpecialtyRequest;
import school.hei.demo.domain.dto.response.SpecialtyResponse;
import school.hei.demo.endpoint.rest.controller.SpecialtyController;
import school.hei.demo.enums.CodeType;
import school.hei.demo.exception.BadRequestException;
import school.hei.demo.exception.ConflictException;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.service.SpecialtyService;

@ExtendWith(MockitoExtension.class)
class SpecialtyControllerTest {
  @Mock SpecialtyService service;
  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new SpecialtyController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    objectMapper = new ObjectMapper().findAndRegisterModules();
  }

  @Test
  void shouldCreateListUpdateAndDelete() throws Exception {
    UUID id = UUID.randomUUID();
    SpecialtyResponse specialty = new SpecialtyResponse(id, CodeType.EL, "Software");
    SpecialtyRequest request = new SpecialtyRequest(CodeType.EL, "Software");
    when(service.list()).thenReturn(List.of(specialty));
    when(service.create(any(SpecialtyRequest.class))).thenReturn(specialty);
    when(service.update(org.mockito.ArgumentMatchers.eq(id), any(SpecialtyRequest.class)))
        .thenReturn(specialty);
    mockMvc
        .perform(get("/specialties"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].codeType").value("EL"));
    mockMvc
        .perform(
            post("/specialties")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
    mockMvc
        .perform(
            patch("/specialties/{id}", id)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
    mockMvc.perform(delete("/specialties/{id}", id)).andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnExpectedErrors() throws Exception {
    UUID id = UUID.randomUUID();
    lenient().when(service.get(id)).thenThrow(new NotFoundException("missing"));
    lenient()
        .when(service.create(any(SpecialtyRequest.class)))
        .thenThrow(new ConflictException("duplicate"));
    lenient()
        .doThrow(new BadRequestException("invalid"))
        .when(service)
        .create((SpecialtyRequest) null);
    SpecialtyRequest request = new SpecialtyRequest(CodeType.EL, "Software");
    mockMvc.perform(get("/specialties/{id}", id)).andExpect(status().isNotFound());
    mockMvc
        .perform(post("/specialties").contentType("application/json").content("null"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            post("/specialties")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }
}
