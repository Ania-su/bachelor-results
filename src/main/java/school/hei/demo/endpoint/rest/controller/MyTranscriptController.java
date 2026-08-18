package school.hei.demo.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.demo.domain.dto.response.StudentTranscriptResponse;
import school.hei.demo.service.CurrentUserService;
import school.hei.demo.service.StudentTranscriptService;

@RestController
@AllArgsConstructor
public class MyTranscriptController {
  private final CurrentUserService currentUserService;
  private final StudentTranscriptService studentTranscriptService;

  @GetMapping("/me/transcript-email")
  public StudentTranscriptResponse getTranscript(
      @RequestParam(required = false) Integer year) {
    return studentTranscriptService.get(currentUserService.getCurrentUser().getId(), year);
  }
}
