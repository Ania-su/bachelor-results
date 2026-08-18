package school.hei.demo.endpoint.rest.controller;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.service.CurrentUserService;
import school.hei.demo.service.StudentTranscriptService;

@RestController
@AllArgsConstructor
public class TranscriptController {
  private final StudentTranscriptService studentTranscriptService;
  private final CurrentUserService currentUserService;

  @GetMapping("/students/{studentId}/transcript")
  public StudentTranscriptResponse getTranscript(
      @PathVariable UUID studentId, @RequestParam(required = false) Integer year) {
    return studentTranscriptService.get(studentId, year);
  }

  @GetMapping("/me/transcript-email")
  public String sendMyTranscript(@RequestParam(required = false) Integer year) {
    return studentTranscriptService.sendTranscriptByEmail(
        currentUserService.getCurrentUser().getId(), year);
  }
}
