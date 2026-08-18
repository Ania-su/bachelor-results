package school.hei.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import school.hei.demo.domain.dto.response.CourseAverageResponse;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.YearTranscriptResponse;
import school.hei.demo.endpoint.rest.controller.TranscriptController;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.GlobalExceptionHandler;
import school.hei.demo.service.CurrentUserService;
import school.hei.demo.service.StudentTranscriptService;

@ExtendWith(MockitoExtension.class)
class TranscriptControllerTest {
  private static final UUID STUDENT_ID = UUID.randomUUID();
  private static final String EMAIL_MESSAGE = "Check your email for your requested transcript.";

  @Mock StudentTranscriptService studentTranscriptService;
  @Mock CurrentUserService currentUserService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new TranscriptController(studentTranscriptService, currentUserService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void getTranscript_returnsTranscriptPojo() throws Exception {
    var transcript = sampleTranscript();
    when(studentTranscriptService.get(STUDENT_ID, 1)).thenReturn(transcript);

    mockMvc
        .perform(get("/students/{studentId}/transcript", STUDENT_ID).param("year", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.reference").value("STD24001"))
        .andExpect(jsonPath("$.isOfficial").value(true));

    verify(studentTranscriptService).get(STUDENT_ID, 1);
  }

  @Test
  void sendMyTranscript_returnsEmailMessage() throws Exception {
    var currentUser = new school.hei.demo.entity.User();
    currentUser.setId(STUDENT_ID);
    when(currentUserService.getCurrentUser()).thenReturn(currentUser);
    when(studentTranscriptService.sendTranscriptByEmail(STUDENT_ID, null))
        .thenReturn(EMAIL_MESSAGE);

    MvcResult result =
        mockMvc.perform(get("/me/transcript-email")).andExpect(status().isOk()).andReturn();

    assertEquals(EMAIL_MESSAGE, result.getResponse().getContentAsString());
    verify(studentTranscriptService).sendTranscriptByEmail(STUDENT_ID, null);
  }

  private StudentTranscriptResponse sampleTranscript() {
    var course =
        new CourseAverageResponse(UUID.randomUUID(), "WEB", "Développement Web", 1, 5, 12.5);
    var year = new YearTranscriptResponse(1, List.of(course), 12.5);
    var user =
        new User(
            STUDENT_ID,
            "STD24001",
            "Rakoto",
            "Jean",
            "rakoto@hei.mg",
            UserRole.STUDENT,
            UUID.randomUUID(),
            2024,
            Instant.parse("2024-09-01T08:00:00Z"));
    return new StudentTranscriptResponse(user, List.of(year), true, 5, 5);
  }
}
