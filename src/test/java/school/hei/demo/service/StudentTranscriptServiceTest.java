package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.demo.domain.dto.response.CourseAverageResponse;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.YearTranscriptResponse;
import school.hei.demo.domain.mappers.UserMapper;
import school.hei.demo.endpoint.event.EventProducer;
import school.hei.demo.endpoint.event.model.SendEmailRequested;
import school.hei.demo.enums.UserRole;
import school.hei.demo.exception.NotFoundException;
import school.hei.demo.file.bucket.BucketComponent;
import school.hei.demo.repository.CourseRepository;
import school.hei.demo.repository.GradeRepository;
import school.hei.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StudentTranscriptServiceTest {
  private static final UUID STUDENT_ID = UUID.randomUUID();
  private static final Integer YEAR = 1;
  private static final String BUCKET_KEY = "transcripts/" + STUDENT_ID + ".pdf";
  private static final String FAKE_URL =
      "https://fake-bucket.s3.amazonaws.com/transcripts/" + STUDENT_ID + ".pdf?signature=abc";

  @BeforeEach
  void setUp() {}

  @Test
  void sendTranscriptByEmail_uploadsGeneratedPdfEmitsEventAndReturnsMessage() throws Exception {
    var transcript = sampleTranscript();
    var student = transcript.getUser();
    var bucketComponent = mock(BucketComponent.class);
    var htmlService = mock(TranscriptHtmlService.class);
    EventProducer<SendEmailRequested> eventProducer = mock(EventProducer.class);

    var service =
        spy(
            new StudentTranscriptService(
                mock(UserRepository.class),
                mock(CourseRepository.class),
                mock(GradeRepository.class),
                mock(UserMapper.class),
                mock(StudentCourseGradeService.class),
                bucketComponent,
                htmlService,
                eventProducer));
    doReturn(transcript).when(service).get(STUDENT_ID, YEAR);
    when(htmlService.toHtml(any(StudentTranscriptResponse.class)))
        .thenReturn("<html><body><h1>Transcript</h1></body></html>");
    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(URI.create(FAKE_URL).toURL());

    String result = service.sendTranscriptByEmail(STUDENT_ID, YEAR);

    assertEquals("Check your email for your requested transcript.", result);
    verify(htmlService).toHtml(same(transcript));

    var fileCaptor = ArgumentCaptor.forClass(java.io.File.class);
    verify(bucketComponent).upload(fileCaptor.capture(), eq(BUCKET_KEY));
    var uploadedFile = fileCaptor.getValue();
    try {
      var content =
          new String(Files.readAllBytes(uploadedFile.toPath()), StandardCharsets.ISO_8859_1);
      assertTrue(content.startsWith("%PDF"), "uploaded file should be a valid PDF");
    } finally {
      uploadedFile.delete();
    }

    verify(bucketComponent).presign(eq(BUCKET_KEY), eq(Duration.ofMinutes(10)));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<SendEmailRequested>> eventCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(eventProducer).accept(eventCaptor.capture());
    var events = eventCaptor.getValue();
    assertEquals(1, events.size());
    var event = events.iterator().next();
    assertEquals(student.getEmail(), event.getTo());
    assertEquals(student.getReference() + " transcript", event.getSubject());
    assertTrue(event.getBody().contains(FAKE_URL));
  }

  @Test
  void sendTranscriptByEmail_escapesLastNameInEmailBody() throws Exception {
    var transcript = sampleTranscript();
    transcript.getUser().setLastName("<script>alert('x')</script>");
    var bucketComponent = mock(BucketComponent.class);
    var htmlService = mock(TranscriptHtmlService.class);
    when(htmlService.escape(anyString())).thenCallRealMethod();
    EventProducer<SendEmailRequested> eventProducer = mock(EventProducer.class);

    var service =
        spy(
            new StudentTranscriptService(
                mock(UserRepository.class),
                mock(CourseRepository.class),
                mock(GradeRepository.class),
                mock(UserMapper.class),
                mock(StudentCourseGradeService.class),
                bucketComponent,
                htmlService,
                eventProducer));
    doReturn(transcript).when(service).get(STUDENT_ID, YEAR);
    when(htmlService.toHtml(any(StudentTranscriptResponse.class)))
        .thenReturn("<html><body></body></html>");
    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(URI.create(FAKE_URL).toURL());

    service.sendTranscriptByEmail(STUDENT_ID, YEAR);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<SendEmailRequested>> eventCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(eventProducer).accept(eventCaptor.capture());
    var body = eventCaptor.getValue().iterator().next().getBody();
    assertTrue(body.contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;"));
  }

  @Test
  void sendTranscriptByEmail_propagatesNotFoundAndDoesNothingElse() {
    var bucketComponent = mock(BucketComponent.class);
    var htmlService = mock(TranscriptHtmlService.class);
    EventProducer<SendEmailRequested> eventProducer = mock(EventProducer.class);

    var service =
        spy(
            new StudentTranscriptService(
                mock(UserRepository.class),
                mock(CourseRepository.class),
                mock(GradeRepository.class),
                mock(UserMapper.class),
                mock(StudentCourseGradeService.class),
                bucketComponent,
                htmlService,
                eventProducer));
    doThrow(new NotFoundException("Student " + STUDENT_ID + " not found"))
        .when(service)
        .get(STUDENT_ID, YEAR);

    assertThrows(NotFoundException.class, () -> service.sendTranscriptByEmail(STUDENT_ID, YEAR));

    verifyNoInteractions(bucketComponent);
    verifyNoInteractions(eventProducer);
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
