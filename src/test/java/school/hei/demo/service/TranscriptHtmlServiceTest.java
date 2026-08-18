package school.hei.demo.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.demo.domain.dto.response.CourseAverageResponse;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.domain.dto.response.User;
import school.hei.demo.domain.dto.response.YearTranscriptResponse;
import school.hei.demo.enums.UserRole;

class TranscriptHtmlServiceTest {

  private final TranscriptHtmlService service = new TranscriptHtmlService();

  @Test
  void toHtml_containsStudentInfoAndYearSections() {
    String html = service.toHtml(sampleTranscript());

    assertTrue(html.contains("<html>"));
    assertTrue(html.contains("Relevé de notes"));
    assertTrue(html.contains("Rakoto Jean"));
    assertTrue(html.contains("STD24001"));
    assertTrue(html.contains("rakoto@hei.mg"));
    assertTrue(html.contains("2024"));
    assertTrue(html.contains("Année 1"));
    assertTrue(html.contains("WEB"));
    assertTrue(html.contains("Développement Web"));
    assertTrue(html.contains("Crédits totaux"));
    assertTrue(html.contains("Crédits validés"));
    assertTrue(html.contains("Moyenne annuelle"));
  }

  @Test
  void toHtml_formatsAveragesWithTwoDecimalsAndShowsCredits() {
    String html = service.toHtml(sampleTranscript());

    assertTrue(html.contains("12,50"));
    assertTrue(html.contains("14,00"));
    assertTrue(html.contains("Officiel"));
  }

  @Test
  void toHtml_marksUnofficialWhenIsOfficialIsFalse() {
    var transcript = sampleTranscript();
    transcript.setIsOfficial(false);

    String html = service.toHtml(transcript);

    assertTrue(html.contains("Non officiel"));
  }

  @Test
  void toHtml_escapesHtmlInStudentAndCourseValues() {
    var transcript = sampleTranscript();
    transcript.getUser().setLastName("<script>alert('x')</script>");
    transcript.getYears().get(0).getCourses().get(0).setTitle("Cours <b>Compliqué</b> & \"C\"");
    transcript.getUser().setFirstName("R&R");

    String html = service.toHtml(transcript);

    assertFalse(html.contains("<script>"));
    assertTrue(html.contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;"));
    assertTrue(html.contains("Cours &lt;b&gt;Compliqué&lt;/b&gt; &amp; &quot;C&quot;"));
    assertTrue(html.contains("R&amp;R"));
  }

  @Test
  void toHtml_handlesNullAverageWithPlaceholder() {
    var transcript = sampleTranscript();
    transcript.getYears().get(0).getCourses().get(0).setAverage(null);
    transcript.getYears().get(0).setAverageYear(null);

    String html = service.toHtml(transcript);

    assertTrue(html.contains("—"));
  }

  private StudentTranscriptResponse sampleTranscript() {
    var course =
        new CourseAverageResponse(UUID.randomUUID(), "WEB", "Développement Web", 1, 5, 12.5);
    var year = new YearTranscriptResponse(1, List.of(course), 14.0);
    var user =
        new User(
            UUID.randomUUID(),
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
